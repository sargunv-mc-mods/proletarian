package me.sargunvohra.mcmods.proletarian.mixinapi;

import net.minecraft.inventory.CraftingInventory;

public interface ModifiedCraftingTableContainer {
    void proletarian_setCraftingInventory(CraftingInventory newCraftingInv);
}
