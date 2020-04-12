package me.sargunvohra.mcmods.proletarian.profession

import com.google.common.collect.ImmutableSet
import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationBlock
import me.sargunvohra.mcmods.proletarian.id
import me.sargunvohra.mcmods.proletarian.mixin.PointOfInterestTypeAccess
import net.minecraft.sound.SoundEvents
import net.minecraft.util.registry.Registry
import net.minecraft.village.VillagerProfession
import net.minecraft.world.poi.PointOfInterestType

object CustomProfessionInit {

    private val poiId = id("proletarian_poi")
    private val professionId = id("proletarian")

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

    fun register() {
        CraftingStationBlock.stateManager.states.forEach {
            PointOfInterestTypeAccess.proletarian_getStatePoiMap()[it] = poiType
        }
        Registry.register(Registry.POINT_OF_INTEREST_TYPE, poiId, poiType)
        Registry.register(Registry.VILLAGER_PROFESSION, professionId, profession)
    }
}
