package me.sargunvohra.mcmods.proletarian.mixin;

import me.sargunvohra.mcmods.proletarian.mixinapi.NamedVillager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {
	@ModifyArg(method = "renderLabel(Lnet/minecraft/entity/Entity;DDDLjava/lang/String;D)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabel(Lnet/minecraft/entity/Entity;Ljava/lang/String;DDDI)V"))
	private String injectVillagerName(T entity, String original, double x, double y, double z, int distance) {
		if (entity instanceof NamedVillager) return ((NamedVillager)entity).getRenderedName();
		else return original;
	}
}
