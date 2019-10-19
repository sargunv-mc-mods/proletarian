package me.sargunvohra.mcmods.proletarian.profession

import me.sargunvohra.mcmods.proletarian.canMergeWith
import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationBlockEntity
import me.sargunvohra.mcmods.proletarian.mixin.VillagerEntityAccess
import me.sargunvohra.mcmods.proletarian.neighbors
import net.minecraft.entity.ai.brain.BlockPosLookTarget
import net.minecraft.entity.ai.brain.MemoryModuleState
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.task.Task
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeType
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.DefaultedList
import net.minecraft.util.ItemScatterer
import net.minecraft.util.Timestamp
import net.minecraft.village.VillageGossipType
import net.minecraft.world.GameRules
import java.util.*

class CraftTask : Task<VillagerEntity>(
    mapOf(
        MemoryModuleType.LOOK_TARGET to MemoryModuleState.REGISTERED,
        MemoryModuleType.WALK_TARGET to MemoryModuleState.VALUE_ABSENT,
        MemoryModuleType.JOB_SITE to MemoryModuleState.VALUE_PRESENT,
        CustomProfessionInit.lastEatenModule to MemoryModuleState.REGISTERED
    ),
    BASE_DELAY
) {

    private var nextCraftTime = 0L
    private lateinit var targetStation: CraftingStationBlockEntity

    private val UNION_UUID = UUID.fromString("3c11d54e-f8cd-45d7-8cff-6e99fab0fb49")

    override fun shouldRun(world: ServerWorld, villager: VillagerEntity): Boolean {
        // apply cool-down time
        if (world.time < nextCraftTime)
            return false

        // we are the right profession
        if (villager.villagerData.profession != CustomProfessionInit.profession)
            return false

        // we are allowed to interact with the world
        if (!world.gameRules.getBoolean(GameRules.MOB_GRIEFING))
            return false

        // we are at our job site
        val jobSite = villager.brain.getOptionalMemory(MemoryModuleType.JOB_SITE).get()
        if (villager.dimension != jobSite.dimension)
            return false
        val myPos = villager.blockPos
        val usablePosSet = myPos.neighbors.filter { it != myPos.up() }
        if (!usablePosSet.contains(jobSite.pos))
            return false

        // we are not focused on anything other than our job site
        val lookTarget = villager.brain.getOptionalMemory(MemoryModuleType.LOOK_TARGET).orElse(null) ?: null
        if (lookTarget != null && lookTarget.blockPos != jobSite.pos)
            return false

        // our job site is the right type
        val station = world.getBlockEntity(jobSite.pos) as? CraftingStationBlockEntity
            ?: return false

        // parameters that affect our happiness
        val access = villager as VillagerEntityAccess

        // we've slept recently
        val slept: Optional<Timestamp> = villager.brain.getOptionalMemory(MemoryModuleType.LAST_SLEPT) as Optional<Timestamp>
        if (!slept.isPresent || villager.world.time - slept.get().time > 24000L) {
            complain(villager, VillageGossipType.MAJOR_NEGATIVE)
            return false
        }

        // we've eaten food recently, or have food to eat otherwise
        val eaten: Optional<Timestamp> = villager.brain.getOptionalMemory(CustomProfessionInit.lastEatenModule) as Optional<Timestamp>
        if (!eaten.isPresent || villager.world.time - eaten.get().time > 3000L) {
            access.proletarian_consumeAvailableFood()
            if (access.proletarian_getFoodLevel() < 1) {
                complain(villager, VillageGossipType.MAJOR_NEGATIVE)
                return false
            } else {
                villager.brain.putMemory(CustomProfessionInit.lastEatenModule, Timestamp.of(villager.world.time))
                access.proletarian_depleteFood(1)
            }
        }

        // we've gossipped in the past three days
        if (villager.world.time - access.proletarian_getGossipStartTime() > 72000L) {
            complain(villager, VillageGossipType.MINOR_NEGATIVE)
            return false
        }

        targetStation = station
        return true
    }

    //complain, and shift gossip further towards unionization (NYI)
    private fun complain(villager: VillagerEntity, type: VillageGossipType) {
        val access = villager as VillagerEntityAccess
        if (villager.world.time % 40 == 0L) {
            access.proletarian_sayNo()
            access.proletarian_getGossips().startGossip(UNION_UUID, type, 1)
        }
    }

    private val CraftingStationBlockEntity.recipeManager get() = world?.server?.recipeManager

    private fun CraftingStationBlockEntity.getCurrentRecipe() =
        recipeManager?.getFirstMatch(RecipeType.CRAFTING, craftingInv, world)?.orElse(null)

    private fun CraftingStationBlockEntity.hasUsableRecipe(): Boolean {
        // do we have a recipe
        val recipe = getCurrentRecipe() ?: return false

        // are the ingredients stackable
        for (i in 0 until craftingInv.invSize) {
            val stack = craftingInv.getInvStack(i)
            if (!stack.isEmpty && !stack.isStackable)
                return false
        }

        // we're good as long as the recipe actually crafts something
        return !recipe.output.isEmpty
    }

    private enum class InsertResult {
        INSERTED,
        FAILED,
        COMPLETED
    }

    private fun CraftingStationBlockEntity.tryInsertIngredient(): InsertResult {
        // search through the crafting grid
        for (craftingIndex in 0 until craftingInv.invSize) {
            val stack = craftingInv.getInvStack(craftingIndex)
            if (stack.isEmpty) continue

            // if we find a stack of one, make it two using an item from the internal inventory
            if (stack.count <= 1) {

                // search the internal inventory
                for (internalIndex in 0 until invSize) {

                    // if found one that can merge, pull it out and merge it
                    if (stack.canMergeWith(getInvStack(internalIndex))) {
                        takeInvStack(internalIndex, 1)
                        stack.increment(1)
                        return InsertResult.INSERTED
                    }
                }

                // didn't find anything inside, complain
                return InsertResult.FAILED
            }
        }

        // everything was stacked to 2+, we're good here
        return InsertResult.COMPLETED
    }

    private fun CraftingStationBlockEntity.craft(): Pair<ItemStack, DefaultedList<ItemStack>> {
        val recipe = getCurrentRecipe()!!

        // create result
        val resultAndRemaining = Pair(
            recipe.craft(craftingInv),
            recipeManager?.getRemainingStacks(RecipeType.CRAFTING, craftingInv, world)
                ?: DefaultedList.of()
        )

        // use up ingredients
        for (craftingIndex in 0 until craftingInv.invSize) {
            val stack = craftingInv.getInvStack(craftingIndex)
            if (stack.isEmpty) continue
            stack.increment(-1)
        }

        markDirty()

        return resultAndRemaining
    }

    override fun run(world: ServerWorld, villager: VillagerEntity, time: Long) {
        val delay = if (villager.hasStatusEffect(StatusEffects.SPEED)) BASE_DELAY else BASE_DELAY * 2
        nextCraftTime = time + delay * 2

        if (!targetStation.hasUsableRecipe())
            return

        villager.brain.putMemory(MemoryModuleType.LOOK_TARGET, BlockPosLookTarget(targetStation.pos))

        when (targetStation.tryInsertIngredient()) {
            InsertResult.INSERTED -> {
                villager.playSound(SoundEvents.ENTITY_ITEM_PICKUP, .5f, 1f)
            }
            InsertResult.FAILED -> {
                if (villager.rand.nextBoolean())
                    villager.playAmbientSound()
            }
            InsertResult.COMPLETED -> {
                val pos = villager.pos
                val (result, remaining) = targetStation.craft()
                ItemScatterer.spawn(world, pos.x, pos.y, pos.z, result)
                remaining.forEach {
                    if (!it.isEmpty)
                        ItemScatterer.spawn(world, pos.x, pos.y, pos.z, it)
                }
                villager.playWorkSound()
            }
        }
    }

    companion object {
        private const val BASE_DELAY = 10
    }
}
