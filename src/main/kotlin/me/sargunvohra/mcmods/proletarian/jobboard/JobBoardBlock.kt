package me.sargunvohra.mcmods.proletarian.jobboard

import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.EntityType
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateFactory
import net.minecraft.state.property.Properties
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
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
        defaultState = this.stateFactory.defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
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
            val server: ServerWorld = world as ServerWorld
            val poiPos: Optional<BlockPos> = server.pointOfInterestStorage.getNearestPosition(
                { true },
                { true },
                pos,
                30,
                PointOfInterestStorage.OccupationStatus.HAS_SPACE
            )

            if (poiPos.isPresent) {
                val newPos = getBlockPos(world, pos, random)
                val villager = VillagerEntity(EntityType.VILLAGER, world, VillagerType.forBiome(world.getBiome(newPos)))
                villager.setPosition(newPos.x.toDouble(), newPos.y.toDouble(), newPos.z.toDouble())
                world.spawnEntity(villager)
            }
        }
    }

    private fun getBlockPos(world: World?, pos: BlockPos?, random: Random?): BlockPos {
        var offsetX = random!!.nextInt(10) + 15
        offsetX = if (random.nextBoolean()) offsetX else offsetX * -1
        var offsetZ = random.nextInt(10) + 15
        offsetZ = if (random.nextBoolean()) offsetZ else offsetZ * -1
        var newPos = BlockPos(pos!!.x + offsetX, pos.y, pos.z + offsetZ)
        for (i in 0..7) {
            if (!world!!.getBlockState(newPos).isAir) newPos = pos.up()
        }
        for (i in 0..5) {
            if (world!!.getBlockState(newPos.down()).isAir) newPos = newPos.down()
        }
        return newPos
    }

    override fun activate(state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): Boolean {
        if (player!!.getStackInHand(hand).item == Items.EMERALD_BLOCK) {
            if (!player.isCreative) player.getStackInHand(hand).decrement(1)
            val random = Random()
            if (random.nextInt(5) == 0) {
                state!!.onRandomTick(world, pos, random)
                val height = state.getOutlineShape(world, pos).method_1102(Direction.Axis.Y, 0.5, 0.5) + 0.03125
                for (i in 0..9) {
                    val spreadX = random.nextGaussian() * 0.02
                    val spreadY = random.nextGaussian() * 0.02
                    val spreadZ = random.nextGaussian() * 0.02
                    world!!.addParticle(ParticleTypes.HAPPY_VILLAGER, pos!!.x.toDouble() + 0.13124999403953552 + 0.737500011920929 * random.nextFloat().toDouble(), pos.y.toDouble() + height + random.nextFloat().toDouble() * (1.0 - height), pos.z.toDouble() + 0.13124999403953552 + 0.737500011920929 * random.nextFloat().toDouble(), spreadX, spreadY, spreadZ)
                }
            }
        }
        return true
    }
}
