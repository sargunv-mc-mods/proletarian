package me.sargunvohra.mcmods.proletarian.craftingstation

import me.sargunvohra.mcmods.proletarian.id
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.gui.container.ContainerScreen54
import net.minecraft.client.gui.container.CraftingTableScreen
import net.minecraft.container.BlockContext
import net.minecraft.container.GenericContainer
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.block.BlockItem
import net.minecraft.util.registry.Registry

object CraftingStationInit {

    val ID = id("crafting_station")
    val CRAFTING_ID = id("crafting_station_crafting_container")
    val STORAGE_ID = id("crafting_station_storage_container")
    val BLOCK_ENTITY_TYPE = BlockEntityType.Builder.create {
        CraftingStationBlockEntity()
    }.build(null)!!

    fun register() {
        val item = BlockItem(CraftingStationBlock, Item.Settings().itemGroup(ItemGroup.DECORATIONS))

        Registry.register(Registry.BLOCK, ID, CraftingStationBlock)
        Registry.register(Registry.ITEM, ID, item)
        Registry.register(Registry.BLOCK_ENTITY, ID, BLOCK_ENTITY_TYPE)

        ContainerProviderRegistry.INSTANCE.registerFactory(CRAFTING_ID) { syncId, _, player, buf ->
            val pos = buf.readBlockPos()
            val entity = player.world.getBlockEntity(pos) as CraftingStationBlockEntity
            PersistentCraftingContainer(
                syncId,
                player.inventory,
                entity.craftingInv,
                BlockContext.create(player.world, pos)
            )
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(STORAGE_ID) { syncId, _, player, buf ->
            val pos = buf.readBlockPos()
            val entity = player.world.getBlockEntity(pos) as CraftingStationBlockEntity
            GenericContainer.createGeneric9x3(syncId, player.inventory, entity)
        }
    }

    fun registerClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(CRAFTING_ID) { syncId, _, player, buf ->
            val pos = buf.readBlockPos()
            val entity = player.world.getBlockEntity(pos) as CraftingStationBlockEntity
            CraftingTableScreen(
                PersistentCraftingContainer(
                    syncId,
                    player.inventory,
                    entity.craftingInv,
                    BlockContext.create(player.world, pos)
                ),
                player.inventory,
                buf.readTextComponent()
            )
        }

        ScreenProviderRegistry.INSTANCE.registerFactory(STORAGE_ID) { syncId, _, player, buf ->
            val pos = buf.readBlockPos()
            val entity = player.world.getBlockEntity(pos) as CraftingStationBlockEntity
            ContainerScreen54(
                GenericContainer.createGeneric9x3(syncId, player.inventory, entity),
                player.inventory,
                buf.readTextComponent()
            )
        }
    }
}
