package me.sargunvohra.mcmods.proletarian.craftingstation

import me.sargunvohra.mcmods.proletarian.mixinapi.ModifiedCraftingInventory
import me.sargunvohra.mcmods.proletarian.mixinapi.ModifiedCraftingTableContainer
import net.minecraft.container.BlockContext
import net.minecraft.container.Container
import net.minecraft.container.CraftingTableContainer
import net.minecraft.container.Slot
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

class PersistentCraftingContainer(
    syncId: Int,
    playerInv: PlayerInventory,
    private val craftingInv: CraftingInventory,
    private val worldPos: BlockContext
) : CraftingTableContainer(syncId, playerInv, worldPos) {

    init {
        @Suppress("CAST_NEVER_SUCCEEDS")
        (this as ModifiedCraftingTableContainer).setCraftingInventory(craftingInv)
        @Suppress("UNCHECKED_CAST")
        (craftingInv as ModifiedCraftingInventory).setContainer(this)
    }

    override fun onContentChanged(inventory: Inventory) {
        super.onContentChanged(inventory)
        worldPos.run { world, pos ->
            (world.getBlockEntity(pos) as? CraftingStationBlockEntity)?.markDirty()
        }
    }

    override fun close(player: PlayerEntity) {
        // super.super.close()
        // we deliberately bypass super's close() because it drops the items from the grid
        val playerInv = player.inventory
        if (!playerInv.cursorStack.isEmpty) {
            player.dropItem(playerInv.cursorStack, false)
            playerInv.cursorStack = ItemStack.EMPTY
        }

        @Suppress("UNCHECKED_CAST")
        (craftingInv as ModifiedCraftingInventory).setContainer(null)
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
}
