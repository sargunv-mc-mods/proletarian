package me.sargunvohra.mcmods.proletarian.craftingstation

import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.GlStateManager
import me.sargunvohra.mcmods.proletarian.rotateRenderState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GuiLighting
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.state.property.Properties

object CraftingStationRenderer : BlockEntityRenderer<CraftingStationBlockEntity>() {

    override fun render(
        entity: CraftingStationBlockEntity,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float,
        destroyStage: Int
    ) {
        super.render(entity, x, y, z, partialTicks, destroyStage)

        val lightmapIndex = this.world.getLightmapIndex(entity.pos.up(), 0)
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (lightmapIndex % 65536).toFloat(), (lightmapIndex / 65536).toFloat())

        GlStateManager.pushMatrix()
        GlStateManager.translated(x, y, z)
        GlStateManager.disableRescaleNormal()

        entity.world?.getBlockState(entity.pos)?.let { state ->
            if (state.block is CraftingStationBlock) {
                rotateRenderState(state.get(Properties.FACING_HORIZONTAL))

                (0 until 9)
                    .map { slot -> slot to entity.craftingInv.getInvStack(slot) }
                    .filter { (_, stack) -> !stack.isEmpty }
                    .forEach { (slot, stack) ->
                        val row = slot % 3
                        val col = slot / 3

                        GlStateManager.pushMatrix()
                        GuiLighting.enable()
                        GlStateManager.enableLighting()
                        GlStateManager.translated(.69 - .19 * row, 1.07, .69 - .19 * col)

                        if (MinecraftClient.getInstance().itemRenderer.getModel(stack).hasDepthInGui()) {
                            GlStateManager.rotated(-90.0, 0.0, 1.0, 0.0)
                        } else {
                            GlStateManager.translated(0.0, -0.064, 0.0)
                            GlStateManager.rotated(90.0, 1.0, 0.0, 0.0)
                            GlStateManager.rotated(180.0, 0.0, 1.0, 0.0)
                        }

                        GlStateManager.scaled(.14, .14, .14)
                        MinecraftClient.getInstance().itemRenderer.renderItem(stack, ModelTransformation.Type.NONE)
                        GlStateManager.popMatrix()
                    }
            }
        }

        GlStateManager.popMatrix()
    }
}