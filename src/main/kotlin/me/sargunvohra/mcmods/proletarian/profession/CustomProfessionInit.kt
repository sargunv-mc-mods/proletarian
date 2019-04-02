package me.sargunvohra.mcmods.proletarian.profession

import com.google.common.collect.ImmutableSet
import me.sargunvohra.mcmods.proletarian.construct
import me.sargunvohra.mcmods.proletarian.id
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.sound.SoundEvents
import net.minecraft.tag.BlockTags
import net.minecraft.tag.Tag
import net.minecraft.util.registry.Registry
import net.minecraft.village.PointOfInterestType
import net.minecraft.village.VillagerProfession
import kotlin.reflect.full.isSubclassOf

// TODO add craft task in VillagerEntity.initBrain()
// TODO add completed condition for no recipe available in poi type

object CustomProfessionInit {

    val POI_ID = id("proletarian_poi")
    val PROFESSION_ID = id("proletarian")

    val POI_BLOCK_TAG = construct(
        BlockTags::class.nestedClasses.first { it.isSubclassOf(Tag::class) },
        POI_ID
    )
    val POI_TYPE = construct(
        PointOfInterestType::class,
        POI_ID.toString(), POI_BLOCK_TAG, 1, SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH
    )
    val PROFESSION = construct(
        VillagerProfession::class,
        PROFESSION_ID.toString(), POI_TYPE, ImmutableSet.of<Item>(), ImmutableSet.of<Block>()
    )

    fun register() {
        Registry.register(Registry.POINT_OF_INTEREST_TYPE, POI_ID, POI_TYPE)
        Registry.register(Registry.VILLAGER_PROFESSION, PROFESSION_ID, PROFESSION)
    }
}
