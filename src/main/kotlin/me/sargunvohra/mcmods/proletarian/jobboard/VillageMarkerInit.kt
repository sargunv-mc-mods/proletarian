package me.sargunvohra.mcmods.proletarian.jobboard

import me.sargunvohra.mcmods.proletarian.id
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.registry.Registry

object VillageMarkerInit {
    private val ID = id("village_marker")

    fun register() {
        Registry.register(Registry.BLOCK, ID, VillageMarkerBlock)

        Registry.register(
            Registry.ITEM, ID, BlockItem(VillageMarkerBlock, Item.Settings().group(ItemGroup.DECORATIONS)))
    }

    fun registerClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(VillageMarkerBlock, RenderLayer.getCutout())
    }
}
