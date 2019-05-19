package me.sargunvohra.mcmods.proletarian.mixin;

import net.minecraft.container.Slot;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccess {

    @Accessor(value = "inventory")
    void proletarian_setInventory(Inventory newInventory);
}
