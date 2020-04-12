package me.sargunvohra.mcmods.proletarian.profession

import com.google.common.collect.ImmutableSet
import com.mojang.datafixers.Dynamic
import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationBlock
import me.sargunvohra.mcmods.proletarian.id
import me.sargunvohra.mcmods.proletarian.mixin.PointOfInterestTypeAccess
import net.minecraft.entity.ai.brain.Activity
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.Schedule
import net.minecraft.entity.ai.brain.ScheduleBuilder
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Timestamp
import net.minecraft.util.registry.Registry
import net.minecraft.village.VillagerProfession
import net.minecraft.world.poi.PointOfInterestType
import java.util.*
import java.util.function.Function

object CustomProfessionInit {

    private val poiId = id("proletarian_poi")
    private val professionId = id("proletarian")
    private val lastEatenModuleId = id("last_eaten")
    private val moreWorkScheduleId = id("more_work_schedule")

    private val poiType = PointOfInterestType(
        poiId.toString(),
        ImmutableSet.copyOf(CraftingStationBlock.stateManager.states),
        1, // ticketCount
        1 // searchDistance
    )

    val profession = VillagerProfession(
        professionId.toString(),
        poiType,
        ImmutableSet.of(),
        ImmutableSet.of(),
        SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH
    )

    val lastEatenModule = MemoryModuleType(Optional.of(Function { d: Dynamic<*> -> Timestamp.of(d) }))

    val moreWorkSchedule = ScheduleBuilder(Schedule())
        .withActivity(10, Activity.IDLE)
        .withActivity(1000, Activity.WORK)
        .withActivity(9000, Activity.MEET)
        .withActivity(10000, Activity.WORK)
        .withActivity(12000, Activity.REST)
        .build()!!

    fun register() {
        CraftingStationBlock.stateManager.states.forEach {
            PointOfInterestTypeAccess.proletarian_getStatePoiMap()[it] = poiType
        }
        Registry.register(Registry.POINT_OF_INTEREST_TYPE, poiId, poiType)
        Registry.register(Registry.VILLAGER_PROFESSION, professionId, profession)
        Registry.register(Registry.MEMORY_MODULE_TYPE, lastEatenModuleId, lastEatenModule)
        Registry.register(Registry.SCHEDULE, moreWorkScheduleId, moreWorkSchedule)
    }
}
