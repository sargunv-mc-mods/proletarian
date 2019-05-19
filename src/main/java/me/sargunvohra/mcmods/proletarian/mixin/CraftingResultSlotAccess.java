package me.sargunvohra.mcmods.proletarian.mixin;

import net.minecraft.container.CraftingResultSlot;
import net.minecraft.inventory.CraftingInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingResultSlot.class)
public interface CraftingResultSlotAccess {
    @Accessor(value = "craftingInv")
    void proletarian_setCraftingInv(CraftingInventory newCraftingInv);
}
