package me.sargunvohra.mcmods.proletarian.mixin;

import me.sargunvohra.mcmods.proletarian.mixinapi.ModifiedCraftingTableContainer;
import net.minecraft.container.*;
import net.minecraft.inventory.CraftingInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftingTableContainer.class)
public abstract class CraftingTableContainerMixin
        extends CraftingContainer<CraftingInventory>
        implements ModifiedCraftingTableContainer {

    @Mutable
    @Shadow
    @Final
    private CraftingInventory craftingInv;

    public CraftingTableContainerMixin(ContainerType<?> type, int syncId) {
        super(type, syncId);
        throw new IllegalStateException();
    }

    @Override
    public void proletarian_setCraftingInventory(CraftingInventory value) {
        for (Slot slot : slotList) {
            if (slot.inventory == craftingInv) {
                ((SlotAccess) slot).proletarian_setInventory(value);
            }
            if (slot instanceof CraftingResultSlot) {
                ((CraftingResultSlotAccess) slot).proletarian_setCraftingInv(value);
            }
        }
        this.craftingInv = value;
    }
}
