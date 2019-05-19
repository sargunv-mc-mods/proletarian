package me.sargunvohra.mcmods.proletarian.mixinapi;

import net.minecraft.container.Container;

public interface ModifiedCraftingInventory {
    void proletarian_setContainer(Container newContainer);
}
