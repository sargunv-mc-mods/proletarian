package me.sargunvohra.mcmods.proletarian.craftingstation

import me.sargunvohra.mcmods.proletarian.rotateRenderState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.state.property.Properties

class CraftingStationRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<CraftingStationBlockEntity>(dispatcher) {

    private val slotIndices = (0 until 9)

    override fun render(
        entity: CraftingStationBlockEntity?,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        val itemRenderer = MinecraftClient.getInstance().itemRenderer

        val state = entity!!.world!!.getBlockState(entity.pos)
        if (state?.block != CraftingStationBlock) return

        val lightLevel = entity.world!!.getLightLevel(entity.pos.up(), 0)

        matrices!!.push()

        rotateRenderState(state.get(Properties.HORIZONTAL_FACING), matrices)

        for (slot in slotIndices) {
            val stack = entity.craftingInv.getInvStack(slot)
            if (stack.isEmpty) continue

            val row = slot % 3
            val col = slot / 3

            matrices.push()
            matrices.translate(.69 - .19 * row, 1.07, .69 - .19 * col)

            if (itemRenderer.getHeldItemModel(stack, entity.world!!, null).hasDepthInGui()) {
                matrices.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion((-90.0).toFloat()))
//                GLStateManager.rotated(-90.0, 0.0, 1.0, 0.0)
            } else {
                matrices.translate(0.0, -0.064, 0.0)
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90.0.toFloat()))
//                GLStateManager.rotated(90.0, 1.0, 0.0, 0.0)
                matrices.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion(90.0.toFloat()))
//                GLStateManager.rotated(180.0, 0.0, 1.0, 0.0)
            }

            matrices.scale(.14.toFloat(), .14.toFloat(), .14.toFloat())
            itemRenderer.renderItem(stack, ModelTransformation.Type.FIXED, lightLevel, overlay, matrices, vertexConsumers)
            matrices.pop()
        }

        matrices.pop()
    }

}
