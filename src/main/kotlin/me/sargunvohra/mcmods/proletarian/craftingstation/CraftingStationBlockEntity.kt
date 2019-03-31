package me.sargunvohra.mcmods.proletarian.craftingstation

import me.sargunvohra.mcmods.proletarian.invFromTag
import me.sargunvohra.mcmods.proletarian.invToTag
import net.minecraft.block.entity.BlockEntity
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.CraftingInventory
import net.minecraft.inventory.Inventory
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.TextComponent
import net.minecraft.text.TranslatableTextComponent

class CraftingStationBlockEntity(
    val craftingInv: CraftingInventory = CraftingInventory(null, 3, 3),
    val internalInv: Inventory = BasicInventory(27)
) :
    BlockEntity(CraftingStationInit.BLOCK_ENTITY_TYPE),
    Inventory by internalInv {

    var customName: TextComponent? = null
    val name
        get() = customName ?: TranslatableTextComponent("container.proletarian.crafting_station")

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        internalInv.invToTag(tag)
        tag.put("CraftingInventory", craftingInv.invToTag(CompoundTag()))
        return tag
    }

    override fun fromTag(tag: CompoundTag) {
        super.fromTag(tag)
        internalInv.invFromTag(tag)
        craftingInv.invFromTag(tag.getCompound("CraftingInventory"))
    }

    override fun markDirty() {
        super.markDirty()
        internalInv.markDirty()
    }
}
