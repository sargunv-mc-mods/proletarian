package me.sargunvohra.mcmods.proletarian.mixinapi;

import net.minecraft.inventory.CraftingInventory;

public interface ModifiedCraftingTableContainer {
    void setCraftingInventory(CraftingInventory newCraftingInv);
}
