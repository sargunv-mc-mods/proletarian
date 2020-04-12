package me.sargunvohra.mcmods.proletarian.craftingstation

import me.sargunvohra.mcmods.proletarian.mixin.CraftingResultSlotAccess
import me.sargunvohra.mcmods.proletarian.mixin.CraftingTableContainerAccess
import me.sargunvohra.mcmods.proletarian.mixin.SlotAccess
import me.sargunvohra.mcmods.proletarian.mixinapi.ModifiedCraftingInventory
import net.minecraft.container.*
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
        val access = this as CraftingTableContainerAccess
        val currentCraftingInv = access.proletarian_getCraftingInventory()
        for (slot in this.slots) {
            if (slot.inventory === currentCraftingInv) {
                (slot as SlotAccess).proletarian_setInventory(craftingInv)
            }
            if (slot is CraftingResultSlot) {
                (slot as CraftingResultSlotAccess).proletarian_setCraftingInv(craftingInv)
            }
        }
        access.proletarian_setCraftingInventory(craftingInv)
        (craftingInv as ModifiedCraftingInventory).proletarian_setContainer(this)
    }

    override fun onContentChanged(inventory: Inventory) {
        super.onContentChanged(inventory)

        // make sure to save the crafting station when we modify the crafting grid
        worldPos.run { world, pos ->
            world.getBlockEntity(pos)?.markDirty()
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

        (craftingInv as ModifiedCraftingInventory).proletarian_setContainer(null)
    }

    override fun canUse(player: PlayerEntity) =
        Container.canUse(worldPos, player, CraftingStationBlock)

    override fun transferSlot(player: PlayerEntity, slotIndex: Int): ItemStack {
        val myIndices = 1..9
        val playerIndices = 10..45

        val slot: Slot? = this.slots[slotIndex]
        if (slot?.hasStack() == true) {
            val slotStack = slot.stack
            val original = slotStack.copy()

            when (slotIndex) {
                craftingResultSlotIndex -> {
                    worldPos.run { world, _ -> slotStack.item.onCraft(slotStack, world, player) }
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

            if (slotStack.count == original.count) {
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
