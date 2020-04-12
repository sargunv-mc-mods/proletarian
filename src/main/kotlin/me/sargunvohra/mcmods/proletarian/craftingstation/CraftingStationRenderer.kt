package me.sargunvohra.mcmods.proletarian.craftingstation

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.state.property.Properties

class CraftingStationRenderer(dispatcher: BlockEntityRenderDispatcher)
    : BlockEntityRenderer<CraftingStationBlockEntity>(dispatcher) {

    private val slotIndices = (0 until 9)

    override fun render(
        blockEntity: CraftingStationBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val itemRenderer = MinecraftClient.getInstance().itemRenderer
        val blockState = blockEntity.world?.getBlockState(blockEntity.pos)
        if (blockState?.block != CraftingStationBlock) return

        val surfaceLight = WorldRenderer.getLightmapCoordinates(blockEntity.world, blockEntity.pos.up())

        matrices.push()

        matrices.translate(0.5, 1.0, 0.5)
        val rot = blockState.get(Properties.HORIZONTAL_FACING).opposite.asRotation()
        matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-rot))

        for (slotIndex in slotIndices) {
            val itemStack = blockEntity.craftingInv.getInvStack(slotIndex)
            if (itemStack.isEmpty) continue

            val row = slotIndex % 3
            val col = slotIndex / 3

            matrices.push()
            matrices.translate(.19 - .19 * row, .07, .19 - .19 * col)

            if (itemRenderer.models.getModel(itemStack).hasDepth()) {
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90f))
            } else {
                matrices.translate(0.0, -0.064, 0.0)
                matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90f))
                matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f))
            }

            matrices.scale(.14f, .14f, .14f)
            itemRenderer.renderItem(
                itemStack,
                ModelTransformation.Mode.NONE,
                surfaceLight,
                overlay,
                matrices,
                vertexConsumers
            )

            matrices.pop()
        }

        matrices.pop()
    }
}
