package me.sargunvohra.mcmods.proletarian.craftingstation

import me.sargunvohra.mcmods.proletarian.id
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.container.BlockContext
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.block.BlockItem
import net.minecraft.util.registry.Registry

object CraftingStationInit {

    val ID = id("crafting_station")
    val BLOCK_ENTITY_TYPE = BlockEntityType.Builder.create {
        CraftingStationBlockEntity()
    }.build(null)!!

    fun register() {
        val item = BlockItem(CraftingStationBlock, Item.Settings().itemGroup(ItemGroup.DECORATIONS))

        Registry.register(Registry.BLOCK, ID, CraftingStationBlock)
        Registry.register(Registry.ITEM, ID, item)
        ContainerProviderRegistry.INSTANCE.registerFactory(ID) { syncId, _, player, buf ->
            val pos = buf.readBlockPos()
            val entity = player.world.getBlockEntity(pos) as CraftingStationBlockEntity
            CraftingStationContainer(
                syncId,
                player.inventory,
                entity.craftingInv,
                BlockContext.create(player.world, pos)
            )
        }
        Registry.register(Registry.BLOCK_ENTITY, ID, BLOCK_ENTITY_TYPE)
    }

    fun registerClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(ID) { syncId, _, player, buf ->
            val pos = buf.readBlockPos()
            val entity = player.world.getBlockEntity(pos) as CraftingStationBlockEntity
            CraftingStationScreen(
                CraftingStationContainer(
                    syncId,
                    player.inventory,
                    entity.craftingInv,
                    BlockContext.create(player.world, pos)
                ),
                player,
                buf.readTextComponent()
            )
        }
    }
}
