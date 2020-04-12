package me.sargunvohra.mcmods.proletarian.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import me.sargunvohra.mcmods.proletarian.profession.CraftTask;
import me.sargunvohra.mcmods.proletarian.profession.CustomProfessionInit;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(VillagerTaskListProvider.class)
public abstract class VillagerTaskListProviderMixin {

    @Inject(method = "createWorkTasks", at = @At("RETURN"), cancellable = true)
    private static void injectCraftTaskIntoWorkTasks(
            VillagerProfession profession,
            float speed,
            CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir) {
        if (profession.equals(CustomProfessionInit.INSTANCE.getProfession())) {
            ArrayList<Pair<Integer, ? extends Task<? super VillagerEntity>>> ret = Lists.newArrayList(cir.getReturnValue());
            ret.add(Pair.of(5, new CraftTask()));
            cir.setReturnValue(ImmutableList.copyOf(ret));
        }
    }
}
