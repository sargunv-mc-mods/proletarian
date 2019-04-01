package me.sargunvohra.mcmods.proletarian.mixin;

import me.sargunvohra.mcmods.proletarian.mixinapi.MixinSettable;
import net.minecraft.container.Slot;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public abstract class SlotMixin implements MixinSettable<Inventory> {

    @Mutable
    @Shadow
    @Final
    public Inventory inventory;

    @Override
    public void mixinSet(Inventory value) {
        this.inventory = value;
    }
}
