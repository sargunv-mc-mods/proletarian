package me.sargunvohra.mcmods.proletarian.mixin;

import com.google.common.collect.Lists;
import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationContainer;
import net.minecraft.client.recipe.book.ClientRecipeBook;
import net.minecraft.client.recipe.book.RecipeBookGroup;
import net.minecraft.container.CraftingContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {

    @Inject(method = "getGroupsForContainer", at = @At("HEAD"), cancellable = true)
    private static void onGetGroupsForContainer(CraftingContainer container, CallbackInfoReturnable<List<RecipeBookGroup>> cir) {
        if (container instanceof CraftingStationContainer) {
            cir.setReturnValue(
                    Lists.newArrayList(
                            RecipeBookGroup.SEARCH,
                            RecipeBookGroup.EQUIPMENT,
                            RecipeBookGroup.BUILDING_BLOCKS,
                            RecipeBookGroup.MISC,
                            RecipeBookGroup.REDSTONE
                    )
            );
        }
    }
}
