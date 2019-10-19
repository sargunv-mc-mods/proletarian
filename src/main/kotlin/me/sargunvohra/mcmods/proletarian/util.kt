package me.sargunvohra.mcmods.proletarian

import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.DefaultedList
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlin.reflect.KClass
import kotlin.reflect.jvm.isAccessible
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.block.entity.BannerPattern
import net.minecraft.item.Items
import net.minecraft.text.TranslatableText
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.math.Direction

fun id(name: String) = Identifier("proletarian", name)

fun <T : Any> construct(`class`: KClass<T>, vararg args: Any?): T {
    `class`.constructors.forEach { constructor ->
        constructor.isAccessible = true
        try {
            return constructor.call(*args)
        } catch (e: IllegalArgumentException) {
        }
    }
    throw IllegalArgumentException("None of the constructors matched the provided args!")
}

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

fun rotateRenderState(dir: Direction) {
    when {
        dir === Direction.EAST -> {
            GlStateManager.rotated(-90.0, 0.0, 1.0, 0.0)
            GlStateManager.translated(0.0, 0.0, -1.0)
        }
        dir === Direction.SOUTH -> {
            GlStateManager.rotated(-180.0, 0.0, 1.0, 0.0)
            GlStateManager.translated(-1.0, 0.0, -1.0)
        }
        dir === Direction.WEST -> {
            GlStateManager.rotated(-270.0, 0.0, 1.0, 0.0)
            GlStateManager.translated(-1.0, 0.0, 0.0)
        }
    }
}

fun getUnityBanner(): ItemStack {
    val stack = ItemStack(Items.BLACK_BANNER)
    val tag = stack.getOrCreateSubTag("BlockEntityTag")
    val list = BannerPattern.Builder()
        .with(BannerPattern.BRICKS, DyeColor.GRAY)
        .with(BannerPattern.CIRCLE_MIDDLE, DyeColor.LIGHT_GRAY)
        .with(BannerPattern.FLOWER, DyeColor.RED)
        .with(BannerPattern.TRIANGLES_BOTTOM, DyeColor.RED)
        .with(BannerPattern.TRIANGLES_TOP, DyeColor.RED)
        .with(BannerPattern.CURLY_BORDER, DyeColor.RED)
        .with(BannerPattern.BORDER, DyeColor.RED)
        .build()
    tag.put("Patterns", list)
    stack.setCustomName(TranslatableText("block.proletarian.unity_banner").formatted(Formatting.GOLD))
    return stack
}
