package me.sargunvohra.mcmods.proletarian.mixin;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerGossips;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VillagerEntity.class)
public interface VillagerEntityAccess {

    @Invoker(value = "consumeAvailableFood")
    void proletarian_consumeAvailableFood();

    @Invoker(value = "depleteFood")
    void proletarian_depleteFood(int amount);

    @Invoker(value = "sayNo")
    void proletarian_sayNo();

    @Accessor(value = "foodLevel")
    byte proletarian_getFoodLevel();

    @Accessor(value = "gossipStartTime")
    long proletarian_getGossipStartTime();

    @Accessor(value = "gossip")
    VillagerGossips proletarian_getGossips();
}
