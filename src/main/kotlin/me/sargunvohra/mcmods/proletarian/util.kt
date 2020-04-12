package me.sargunvohra.mcmods.proletarian

import net.minecraft.block.entity.BannerPattern
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.TranslatableText
import net.minecraft.util.DefaultedList
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

fun id(name: String) = Identifier("proletarian", name)

fun Inventory.invToTag(tag: CompoundTag): CompoundTag {
    val items = (0 until invSize).map { i -> getInvStack(i) }.toTypedArray()
    return Inventories.toTag(tag, DefaultedList.copyOf(ItemStack.EMPTY, *items))
}

fun Inventory.invFromTag(tag: CompoundTag) {
    val savedContent = DefaultedList.ofSize(invSize, ItemStack.EMPTY)
    Inventories.fromTag(tag, savedContent)
    savedContent.forEachIndexed(this::setInvStack)
}

val BlockPos.neighbors: Set<BlockPos> get() = setOf(north(), south(), east(), west(), down(), up())

fun ItemStack.canMergeWith(other: ItemStack): Boolean {
    return when {
        item !== other.item -> false
        damage != other.damage -> false
        count > maxCount -> false
        else -> ItemStack.areTagsEqual(this, other)
    }
}

fun getUnityBanner(): ItemStack {
    val stack = ItemStack(Items.BLACK_BANNER)
    val tag = stack.getOrCreateSubTag("BlockEntityTag")
    val list = BannerPattern.Patterns()
        .add(BannerPattern.BRICKS, DyeColor.GRAY)
        .add(BannerPattern.CIRCLE_MIDDLE, DyeColor.LIGHT_GRAY)
        .add(BannerPattern.FLOWER, DyeColor.RED)
        .add(BannerPattern.TRIANGLES_BOTTOM, DyeColor.RED)
        .add(BannerPattern.TRIANGLES_TOP, DyeColor.RED)
        .add(BannerPattern.CURLY_BORDER, DyeColor.RED)
        .add(BannerPattern.BORDER, DyeColor.RED)
        .toTag()
    tag.put("Patterns", list)
    stack.setCustomName(TranslatableText("block.proletarian.unity_banner").formatted(Formatting.GOLD))
    return stack
}
