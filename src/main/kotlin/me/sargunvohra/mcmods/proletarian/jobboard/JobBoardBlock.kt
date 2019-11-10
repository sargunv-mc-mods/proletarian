package me.sargunvohra.mcmods.proletarian.jobboard

import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationBlock
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.EntityType
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateFactory
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.village.PointOfInterestStorage
import net.minecraft.village.VillagerType
import net.minecraft.world.World
import java.util.*

@Suppress("OverridingDeprecatedMember")
object JobBoardBlock: Block(
    FabricBlockSettings
        .of(Material.WOOD)
        .strength(2.5F, 2.5F)
        .sounds(BlockSoundGroup.WOOD)
        .ticksRandomly()
        .build()
) {

    init {
        defaultState = CraftingStationBlock.stateFactory.defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
    }

    override fun getPlacementState(context: ItemPlacementContext) =
        defaultState.with(Properties.HORIZONTAL_FACING, context.playerFacing.opposite)!!

    override fun rotate(state: BlockState, rotation: BlockRotation) =
        state.with(Properties.HORIZONTAL_FACING, rotation.rotate(state.get(Properties.HORIZONTAL_FACING)))!!

    override fun mirror(state: BlockState, mirror: BlockMirror) =
        state.rotate(mirror.getRotation(state.get(Properties.HORIZONTAL_FACING)))!!

    override fun appendProperties(stateFactoryBuilder: StateFactory.Builder<Block, BlockState>) {
        stateFactoryBuilder.add(Properties.HORIZONTAL_FACING)
    }

    override fun onRandomTick(state: BlockState?, world: World?, pos: BlockPos?, random: Random?) {
        if (!world!!.isClient) {
            if (random!!.nextInt(10) == 1) {
                val server: ServerWorld = world as ServerWorld
                val poiPos: Optional<BlockPos> = server.pointOfInterestStorage.getNearestPosition(
                    { true },
                    { true },
                    pos,
                    30,
                    PointOfInterestStorage.OccupationStatus.HAS_SPACE
                )

                if (poiPos.isPresent) {
                    val villager = VillagerEntity(EntityType.VILLAGER, world, VillagerType.forBiome(world.getBiome(pos)))
                    val newPos = getBlockPos(world, pos, random)
                    villager.setPosition(newPos.x.toDouble(), newPos.y.toDouble(), newPos.z.toDouble())
                    world.spawnEntity(villager)
                }
            }
        }
    }

    private fun getBlockPos(world: World?, pos: BlockPos?, random: Random?): BlockPos {
        var offsetX = random!!.nextInt(10)
        offsetX = if (random.nextBoolean()) offsetX else offsetX * -1
        var offsetZ = random.nextInt(10)
        offsetZ = if (random.nextBoolean()) offsetZ else offsetZ * -1
        var newPos = BlockPos(pos!!.x + offsetX + 10, pos.y, pos.z + offsetZ + 10)
        for (i in 0..7) {
            if (!world!!.getBlockState(newPos).isAir) newPos = pos.up()
        }
        for (i in 0..5) {
            if (world!!.getBlockState(newPos.down()).isAir) newPos = newPos.down()
        }
        return newPos
    }
}
