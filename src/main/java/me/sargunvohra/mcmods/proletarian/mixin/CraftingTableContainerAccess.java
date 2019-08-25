package me.sargunvohra.mcmods.proletarian.mixin;

import net.minecraft.container.CraftingTableContainer;
import net.minecraft.inventory.CraftingInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingTableContainer.class)
public interface CraftingTableContainerAccess {

    @Accessor(value = "craftingInv")
    void proletarian_setCraftingInventory(CraftingInventory newInventory);

    @Accessor(value = "craftingInv")
    CraftingInventory proletarian_getCraftingInventory();
}
