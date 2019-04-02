package me.sargunvohra.mcmods.proletarian

import com.google.common.collect.ImmutableList
import com.mojang.datafixers.util.Pair
import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationInit
import net.fabricmc.api.ClientModInitializer
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.GoToNearbyPositionTask
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.task.*
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.village.VillagerProfession

object ProletarianClientInit : ClientModInitializer {
    override fun onInitializeClient() {
        CraftingStationInit.registerClient()
    }

    fun getWorkTasks(profession: VillagerProfession, speed: Float): ImmutableList<Pair<Int, out Task<in VillagerEntity>>> {
        return ImmutableList.of(
            Pair.of(5, RandomTask(ImmutableList.of(
                Pair.of<Task<in VillagerEntity>, Int>(VillagerWorkTask(), 7),
                Pair.of<Task<in VillagerEntity>, Int>(GoToIfNearbyTask(MemoryModuleType.JOB_SITE, 4), 2),
                Pair.of<Task<in VillagerEntity>, Int>(GoToNearbyPositionTask(MemoryModuleType.JOB_SITE, 1, 10), 5),
                Pair.of<Task<in VillagerEntity>, Int>(GoToRandomMemorizedPositionTask(MemoryModuleType.SECONDARY_JOB_SITE, 0.4f, 1, 6, MemoryModuleType.JOB_SITE), 5),
                Pair.of<Task<in VillagerEntity>, Int>(FarmerVillagerTask(), 5)
            ))),
            Pair.of(10, InteractTask(400, 1600)),
            Pair.of(10, FindInteractTargetTask(EntityType.PLAYER, 4)),
            Pair.of(2, VillagerWalkTowardsTask(MemoryModuleType.JOB_SITE, speed, 9, 100)),
            Pair.of(3, GiveGiftsTask(100)),
            Pair.of(3, ForgetCompletedPointOfInterestTask(profession.workStation, MemoryModuleType.JOB_SITE)),
            Pair.of(99, ScheduleActivityTask())
        )
    }
}
