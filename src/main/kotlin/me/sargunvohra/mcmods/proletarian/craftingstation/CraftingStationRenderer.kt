package me.sargunvohra.mcmods.proletarian.craftingstation

import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import me.sargunvohra.mcmods.proletarian.rotateRenderState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.state.property.Properties

class CraftingStationRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<CraftingStationBlockEntity>(dispatcher) {

    private val slotIndices = (0 until 9)

    override fun render(
        blockEntity: CraftingStationBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
//        val itemRenderer = MinecraftClient.getInstance().itemRenderer
//        val blockState = blockEntity.world?.getBlockState(blockEntity.pos)
//        if (blockState?.block != CraftingStationBlock) return
//        val world = blockEntity.world!!
//
//        matrices.multiply(blockState.get(Properties.HORIZONTAL_FACING).rotationQuaternion)
    }

//    override fun render(
//        entity: CraftingStationBlockEntity,
//        x: Double,
//        y: Double,
//        z: Double,
//        partialTicks: Float,
//        destroyStage: Int
//    ) {
//        super.render(entity, x, y, z, partialTicks, destroyStage)
//        val itemRenderer = MinecraftClient.getInstance().itemRenderer
//
//        val state = entity.world?.getBlockState(entity.pos)
//        if (state?.block != CraftingStationBlock) return
//
//        val lightmapIndex = this.world.getLightmapIndex(entity.pos.up(), 0)
//        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (lightmapIndex % 65536).toFloat(), (lightmapIndex / 65536).toFloat())
//
//        GlStateManager.pushMatrix()
//        GlStateManager.translated(x, y, z)
//        GlStateManager.disableRescaleNormal()
//
//        rotateRenderState(state.get(Properties.HORIZONTAL_FACING))
//
//        for (slot in slotIndices) {
//            val stack = entity.craftingInv.getInvStack(slot)
//            if (stack.isEmpty) continue
//
//            val row = slot % 3
//            val col = slot / 3
//
//            GlStateManager.pushMatrix()
//            DiffuseLighting.enable()
//            GlStateManager.enableLighting()
//            GlStateManager.translated(.69 - .19 * row, 1.07, .69 - .19 * col)
//
//            if (itemRenderer.getModel(stack).hasDepthInGui()) {
//                GlStateManager.rotated(-90.0, 0.0, 1.0, 0.0)
//            } else {
//                GlStateManager.translated(0.0, -0.064, 0.0)
//                GlStateManager.rotated(90.0, 1.0, 0.0, 0.0)
//                GlStateManager.rotated(180.0, 0.0, 1.0, 0.0)
//            }
//
//            GlStateManager.scaled(.14, .14, .14)
//            itemRenderer.renderItem(stack, ModelTransformation.Type.NONE)
//            GlStateManager.popMatrix()
//        }
//
//        GlStateManager.popMatrix()
//    }
}
