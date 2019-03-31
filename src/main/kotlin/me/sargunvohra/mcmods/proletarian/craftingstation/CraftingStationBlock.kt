package me.sargunvohra.mcmods.proletarian.craftingstation

import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.container.Container
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.sortme.ItemScatterer
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

@Suppress("OverridingDeprecatedMember")
object CraftingStationBlock : BlockWithEntity(
    FabricBlockSettings
        .of(Material.WOOD)
        .strength(2.5f, 2.5f)
        .sounds(BlockSoundGroup.WOOD)
        .build()
) {
    override fun createBlockEntity(blockView: BlockView): BlockEntity {
        return CraftingStationBlockEntity()
    }

    override fun activate(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, blockHitPos: BlockHitResult): Boolean {
        if (!world.isClient) {
            val entity = world.getBlockEntity(pos)
            if (entity is CraftingStationBlockEntity) {
                ContainerProviderRegistry.INSTANCE.openContainer(CraftingStationInit.ID, player) {
                    it.writeBlockPos(pos)
                    it.writeTextComponent(entity.name)
                }
            }
        }

        return true
    }

    override fun onPlaced(world: World, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        if (stack.hasDisplayName()) {
            val entity = world.getBlockEntity(pos)
            if (entity is CraftingStationBlockEntity) {
                entity.customName = stack.displayName
            }
        }
    }

    override fun onBlockRemoved(state1: BlockState, world: World, pos: BlockPos, state2: BlockState, someBool: Boolean) {
        if (state1.block !== state2.block) {
            val entity = world.getBlockEntity(pos)
            if (entity is CraftingStationBlockEntity) {
                ItemScatterer.spawn(world, pos, entity.craftingInv)
                ItemScatterer.spawn(world, pos, entity.internalInv)
                world.updateHorizontalAdjacent(pos, this)
            }
        }
        @Suppress("DEPRECATION")
        super.onBlockRemoved(state1, world, pos, state2, someBool)
    }

    override fun hasComparatorOutput(state: BlockState): Boolean {
        return true
    }

    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        return Container.calculateComparatorOutput(world.getBlockEntity(pos))
    }

    override fun getRenderType(state: BlockState): BlockRenderType {
        return BlockRenderType.MODEL
    }
}
