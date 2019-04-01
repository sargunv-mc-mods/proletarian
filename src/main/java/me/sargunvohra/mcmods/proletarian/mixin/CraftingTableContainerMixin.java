package me.sargunvohra.mcmods.proletarian.mixin;

import me.sargunvohra.mcmods.proletarian.mixinapi.MixinSettable;
import net.minecraft.container.*;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CraftingTableContainer.class)
public abstract class CraftingTableContainerMixin
        extends CraftingContainer<CraftingInventory>
        implements MixinSettable<CraftingInventory> {

    @Mutable
    @Shadow
    @Final
    private CraftingInventory craftingInv;

    public CraftingTableContainerMixin(ContainerType<?> type, int syncId) {
        super(type, syncId);
        throw new IllegalStateException();
    }

    @Override
    public void mixinSet(CraftingInventory value) {
        for (Slot slot : slotList) {
            if (slot.inventory == craftingInv) {
                //noinspection unchecked
                ((MixinSettable<Inventory>) slot).mixinSet(value);
            }
            if (slot instanceof CraftingResultSlot) {
                //noinspection unchecked
                ((MixinSettable<CraftingInventory>)slot).mixinSet(value);
            }
        }
        this.craftingInv = value;
    }
}
