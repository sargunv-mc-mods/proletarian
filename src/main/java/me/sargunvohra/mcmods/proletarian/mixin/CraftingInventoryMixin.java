package me.sargunvohra.mcmods.proletarian.mixin;

import me.sargunvohra.mcmods.proletarian.mixinapi.SetContainer;
import net.minecraft.container.Container;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mixin(CraftingInventory.class)
public abstract class CraftingInventoryMixin implements Inventory, RecipeInputProvider, SetContainer {

    @Shadow
    @Final
    private DefaultedList<ItemStack> stacks;

    @Mutable
    @Shadow
    @Final
    private Container container;

    @Override
    public void setContainer(Container container) {
        this.container = container;
        if (this.container != null) {
            this.container.onContentChanged(this);
        }
    }

    /**
     * @reason need to handle container = null
     * @author sargunv
     */
    @Overwrite
    public void setInvStack(int index, ItemStack stack) {
        this.stacks.set(index, stack);
        if (this.container != null)
            this.container.onContentChanged(this);
    }

    /**
     * @reason need to handle container = null
     * @author sargunv
     */
    @Overwrite
    public ItemStack takeInvStack(int index, int amount) {
        ItemStack stack = Inventories.splitStack(this.stacks, index, amount);
        if (!stack.isEmpty() && this.container != null) {
            this.container.onContentChanged(this);
        }
        return stack;
    }
}
