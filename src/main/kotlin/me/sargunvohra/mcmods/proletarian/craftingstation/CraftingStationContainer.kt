package me.sargunvohra.mcmods.proletarian.craftingstation

import me.sargunvohra.mcmods.proletarian.mixinapi.SetContainer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.network.packet.GuiSlotUpdateS2CPacket
import net.minecraft.container.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeFinder
import net.minecraft.recipe.RecipeType
import net.minecraft.server.network.ServerPlayerEntity

class CraftingStationContainer(
    syncId: Int,
    private val playerInv: PlayerInventory,
    private val craftingInv: CraftingInventory,
    private val worldPos: BlockContext
) : CraftingContainer<CraftingInventory>(ContainerType.CRAFTING, syncId) {

    private val resultInv: CraftingResultInventory = CraftingResultInventory()

    init {
        (craftingInv as SetContainer).setContainer(this)

        // crafting result slot
        addSlot(CraftingResultSlot(playerInv.player, craftingInv, resultInv, 0, 124, 35))

        // crafting grid slots
        for (row in 0..2) {
            for (col in 0..2) {
                addSlot(Slot(craftingInv, col + row * 3, 30 + col * 18, 17 + row * 18))
            }
        }

        // player main inv slots
        for (row in 0..2) {
            for (col in 0..8) {
                addSlot(Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18))
            }
        }

        // player hotbar slots
        for (col in 0..8) {
            addSlot(Slot(playerInv, col, 8 + col * 18, 142))
        }
    }

    override fun onContentChanged(inv: Inventory) {
        worldPos.run { world, _ ->
            if (!world.isClient) {
                val player = playerInv.player as ServerPlayerEntity
                val result = world.server!!.recipeManager
                    .get(RecipeType.CRAFTING, craftingInv, world)
                    .filter { resultInv.shouldCraftRecipe(world, player, it) }
                    .map { it.craft(craftingInv) }
                    .orElse(ItemStack.EMPTY)
                resultInv.setInvStack(0, result)
                player.networkHandler.sendPacket(GuiSlotUpdateS2CPacket(syncId, 0, result))
            }
        }
    }

    override fun populateRecipeFinder(recipeFinder: RecipeFinder) {
        craftingInv.provideRecipeInputs(recipeFinder)
    }

    override fun clearCraftingSlots() {
        craftingInv.clear()
        resultInv.clear()
    }

    override fun matches(recipe: Recipe<in CraftingInventory>) =
        recipe.matches(craftingInv, playerInv.player.world)

    override fun close(player: PlayerEntity) {
        super.close(player)
        (craftingInv as SetContainer).setContainer(null)
    }

    override fun canUse(player: PlayerEntity) =
        Container.canUse(worldPos, player, CraftingStationBlock)

    override fun transferSlot(player: PlayerEntity, slotIndex: Int): ItemStack {
        val myIndices = 1..9
        val playerIndices = 10..45

        val slot: Slot? = slotList[slotIndex]
        if (slot?.hasStack() == true) {
            val slotStack = slot.stack
            val original = slotStack.copy()

            when (slotIndex) {
                craftingResultSlotIndex -> {
                    worldPos.run { world, _ -> slotStack.item.onCrafted(slotStack, world, player) }
                    if (!insertItem(slotStack, playerIndices.first, playerIndices.last + 1, true)) {
                        return ItemStack.EMPTY
                    }
                    slot.onStackChanged(slotStack, original)
                }
                in myIndices -> if (!insertItem(slotStack, playerIndices.first, playerIndices.last + 1, false)) {
                    return ItemStack.EMPTY
                }
                in playerIndices -> if (!insertItem(slotStack, myIndices.first, myIndices.last + 1, false)) {
                    return ItemStack.EMPTY
                }
            }

            if (slotStack.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }

            if (slotStack.amount == original.amount) {
                return ItemStack.EMPTY
            }

            val takenStack = slot.onTakeItem(player, slotStack)
            if (slotIndex == 0) {
                player.dropItem(takenStack, false)
            }

            return original
        }

        return ItemStack.EMPTY
    }

    override fun canInsertIntoSlot(stack: ItemStack, slot: Slot) =
        slot.inventory !== resultInv && super.canInsertIntoSlot(stack, slot)

    override fun getCraftingResultSlotIndex() = 0

    override fun getCraftingWidth() = craftingInv.width

    override fun getCraftingHeight() = craftingInv.height

    @Environment(EnvType.CLIENT)
    override fun getCraftingSlotCount() = 10

}
