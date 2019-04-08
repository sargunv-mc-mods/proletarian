package me.sargunvohra.mcmods.proletarian.profession

import com.mojang.datafixers.util.Pair
import me.sargunvohra.mcmods.proletarian.canMergeWith
import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationBlockEntity
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
import net.minecraft.sortme.ItemScatterer
import net.minecraft.sound.SoundEvents
import net.minecraft.util.DefaultedList

class CraftTask : Task<VillagerEntity>(BASE_DELAY) {

    private var nextCraftTime = 0L
    private lateinit var targetStation: CraftingStationBlockEntity

    private fun memoryStatePair(type: MemoryModuleType<*>, state: MemoryModuleState) =
        Pair.of(type, state)

    override fun getRequiredMemoryState(): Set<Pair<MemoryModuleType<*>, MemoryModuleState>> {
        return setOf(
            memoryStatePair(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED),
            memoryStatePair(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT),
            memoryStatePair(MemoryModuleType.JOB_SITE, MemoryModuleState.VALUE_PRESENT)
        )
    }

    override fun shouldRun(world: ServerWorld, villager: VillagerEntity): Boolean {
        // apply cool-down time
        if (world.time < nextCraftTime)
            return false

        // we are the right profession
        if (villager.villagerData.profession != CustomProfessionInit.profession)
            return false

        // we are allowed to interact with the world
        if (!world.gameRules.getBoolean("mobGriefing"))
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

        targetStation = station
        return true
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
            if (!stack.isEmpty && !stack.canStack())
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
            if (stack.amount <= 1) {

                // search the internal inventory
                for (internalIndex in 0 until invSize) {

                    // if found one that can merge, pull it out and merge it
                    if (stack.canMergeWith(getInvStack(internalIndex))) {
                        takeInvStack(internalIndex, 1)
                        stack.addAmount(1)
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

    private fun CraftingStationBlockEntity.craft(): kotlin.Pair<ItemStack, DefaultedList<ItemStack>> {
        val recipe = getCurrentRecipe()!!

        // create result
        val resultAndRemaining = kotlin.Pair(
            recipe.craft(craftingInv),
            recipeManager?.getRemainingStacks(RecipeType.CRAFTING, craftingInv, world)
                ?: DefaultedList.create()
        )

        // use up ingredients
        for (craftingIndex in 0 until craftingInv.invSize) {
            val stack = craftingInv.getInvStack(craftingIndex)
            if (stack.isEmpty) continue
            stack.addAmount(-1)
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
