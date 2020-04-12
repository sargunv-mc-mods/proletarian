package me.sargunvohra.mcmods.proletarian.mixin;

import com.google.common.collect.ImmutableList;
import me.sargunvohra.mcmods.proletarian.profession.CustomProfessionInit;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends AbstractTraderEntity {

    @Shadow
    @Final
    @Mutable
    private static ImmutableList<MemoryModuleType<?>> MEMORY_MODULES;

    @Shadow
    public abstract VillagerData getVillagerData();

    public VillagerEntityMixin(EntityType<? extends AbstractTraderEntity> type, World world) {
        super(type, world);
    }

    @ModifyArg(method = "initBrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/Brain;setSchedule(Lnet/minecraft/entity/ai/brain/Schedule;)V"))
    private Schedule initBrain(Schedule original) {
        VillagerProfession profession = this.getVillagerData().getProfession();
        if (!this.isBaby() && profession.equals(CustomProfessionInit.INSTANCE.getProfession())) {
            return CustomProfessionInit.INSTANCE.getMoreWorkSchedule();
        }
        return original;
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void clinit(CallbackInfo ci) {
        MEMORY_MODULES = ImmutableList.<MemoryModuleType<?>>builder()
                .addAll(MEMORY_MODULES)
                .add(CustomProfessionInit.INSTANCE.getLastEatenModule())
                .build();
    }
}
