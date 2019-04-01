package me.sargunvohra.mcmods.proletarian.mixin;

import me.sargunvohra.mcmods.proletarian.mixinapi.MixinSettable;
import net.minecraft.container.CraftingResultSlot;
import net.minecraft.container.Slot;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin implements MixinSettable<CraftingInventory> {

    @Mutable
    @Shadow
    @Final
    private CraftingInventory craftingInv;

    @Override
    public void mixinSet(CraftingInventory value) {
        this.craftingInv = value;
    }
}
