package me.sargunvohra.mcmods.proletarian.mixin;

import me.sargunvohra.mcmods.proletarian.mixinapi.MixinSettable;
import net.minecraft.container.Container;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.*;

@Mixin(CraftingInventory.class)
public abstract class CraftingInventoryMixin implements Inventory, RecipeInputProvider, MixinSettable<Container> {

    @Shadow
    @Final
    private DefaultedList<ItemStack> stacks;

    @Mutable
    @Shadow
    @Final
    private Container container;

    @Override
    public void mixinSet(Container value) {
        this.container = value;
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
