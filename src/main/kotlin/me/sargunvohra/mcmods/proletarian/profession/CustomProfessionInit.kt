package me.sargunvohra.mcmods.proletarian.profession

import com.google.common.collect.ImmutableSet
import me.sargunvohra.mcmods.proletarian.construct
import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationBlock
import me.sargunvohra.mcmods.proletarian.id
import me.sargunvohra.mcmods.proletarian.mixin.PointOfInterestTypeAccess
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.sound.SoundEvents
import net.minecraft.util.registry.Registry
import net.minecraft.village.PointOfInterestType
import net.minecraft.village.VillagerProfession

object CustomProfessionInit {

    private val poiId = id("proletarian_poi")
    private val professionId = id("proletarian")

    private val poiType = construct(
        PointOfInterestType::class,
        poiId.toString(), ImmutableSet.copyOf(CraftingStationBlock.stateFactory.states), 1, SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH
    )
    val profession = construct(
        VillagerProfession::class,
        professionId.toString(), poiType, ImmutableSet.of<Item>(), ImmutableSet.of<Block>()
    )

    fun register() {
        CraftingStationBlock.stateFactory.states.forEach {
            PointOfInterestTypeAccess.getStatePoiMap()[it] = poiType
        }
        Registry.register(Registry.POINT_OF_INTEREST_TYPE, poiId, poiType)
        Registry.register(Registry.VILLAGER_PROFESSION, professionId, profession)
    }
}
