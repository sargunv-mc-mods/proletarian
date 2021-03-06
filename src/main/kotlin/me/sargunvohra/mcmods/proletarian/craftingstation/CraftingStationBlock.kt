package me.sargunvohra.mcmods.proletarian.craftingstation

import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.Material
import net.minecraft.container.Container
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.*
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
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

    init {
        defaultState = stateManager.defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
    }

    override fun getPlacementState(context: ItemPlacementContext) =
        defaultState.with(Properties.HORIZONTAL_FACING, context.playerFacing.opposite)!!

    override fun rotate(state: BlockState, rotation: BlockRotation) =
        state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)))!!

    override fun mirror(state: BlockState, mirror: BlockMirror) =
        state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)))!!

    override fun appendProperties(stateFactoryBuilder: StateManager.Builder<Block, BlockState>) {
        stateFactoryBuilder.add(Properties.HORIZONTAL_FACING)
    }

    override fun createBlockEntity(blockView: BlockView) = CraftingStationBlockEntity()

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        blockHitPos: BlockHitResult
    ): ActionResult {
        if (!world.isClient) {
            val container = if (blockHitPos.pos.y - blockHitPos.blockPos.y > .75)
                CraftingStationInit.CRAFTING_ID
            else
                CraftingStationInit.STORAGE_ID

            val entity = world.getBlockEntity(pos)
            if (entity is CraftingStationBlockEntity) {
                ContainerProviderRegistry.INSTANCE.openContainer(container, player) {
                    it.writeBlockPos(pos)
                    it.writeText(entity.name)
                }
            }
        }

        return ActionResult.SUCCESS
    }

    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        stack: ItemStack
    ) {
        if (stack.hasCustomName()) {
            val entity = world.getBlockEntity(pos)
            if (entity is CraftingStationBlockEntity) {
                entity.customName = stack.name
            }
        }
    }

    override fun onBlockRemoved(
        state1: BlockState,
        world: World,
        pos: BlockPos,
        state2: BlockState,
        someBool: Boolean
    ) {
        if (state1.block !== state2.block) {
            val entity = world.getBlockEntity(pos)
            if (entity is CraftingStationBlockEntity) {
                ItemScatterer.spawn(world, pos, entity.craftingInv)
                ItemScatterer.spawn(world, pos, entity)
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
