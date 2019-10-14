package me.sargunvohra.mcmods.proletarian.mixin;

import com.google.common.collect.ImmutableList;
import me.sargunvohra.mcmods.proletarian.profession.CustomProfessionInit;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {
    @Shadow @Final @Mutable private static ImmutableList<MemoryModuleType<?>> MEMORY_MODULES =
            ImmutableList.of(MemoryModuleType.HOME,
                    MemoryModuleType.JOB_SITE,
                    MemoryModuleType.MEETING_POINT,
                    MemoryModuleType.MOBS,
                    MemoryModuleType.VISIBLE_MOBS,
                    MemoryModuleType.VISIBLE_VILLAGER_BABIES,
                    MemoryModuleType.NEAREST_PLAYERS,
                    MemoryModuleType.NEAREST_VISIBLE_PLAYER,
                    MemoryModuleType.WALK_TARGET,
                    MemoryModuleType.LOOK_TARGET,
                    MemoryModuleType.INTERACTION_TARGET,
                    MemoryModuleType.BREED_TARGET,
                    new MemoryModuleType[]{
                            MemoryModuleType.PATH,
                            MemoryModuleType.INTERACTABLE_DOORS,
                            MemoryModuleType.OPENED_DOORS,
                            MemoryModuleType.NEAREST_BED,
                            MemoryModuleType.HURT_BY,
                            MemoryModuleType.HURT_BY_ENTITY,
                            MemoryModuleType.NEAREST_HOSTILE,
                            MemoryModuleType.SECONDARY_JOB_SITE,
                            MemoryModuleType.HIDING_PLACE,
                            MemoryModuleType.HEARD_BELL_TIME,
                            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                            MemoryModuleType.LAST_SLEPT,
                            MemoryModuleType.LAST_WORKED_AT_POI,
                            MemoryModuleType.GOLEM_LAST_SEEN_TIME,
                            CustomProfessionInit.INSTANCE.getLastEatenModule()
            });

}
