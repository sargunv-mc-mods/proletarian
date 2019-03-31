package me.sargunvohra.mcmods.proletarian.craftingstation

import com.mojang.blaze3d.platform.GlStateManager
import me.sargunvohra.mcmods.proletarian.id
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.ContainerScreen
import net.minecraft.client.gui.ingame.RecipeBookProvider
import net.minecraft.client.gui.recipebook.RecipeBookGui
import net.minecraft.client.gui.widget.RecipeBookButtonWidget
import net.minecraft.container.Slot
import net.minecraft.container.SlotActionType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.TextComponent
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
open class CraftingStationScreen(
    container: CraftingStationContainer,
    player: PlayerEntity,
    title: TextComponent
) : ContainerScreen<CraftingStationContainer>(container, player.inventory, title), RecipeBookProvider {

    private val bgTex = Identifier("textures/gui/container/crafting_table.png")
    private val recipeButtonTex = Identifier("textures/gui/recipe_button.png")

    private val recipeBookGui = RecipeBookGui()
    private var isNarrow: Boolean = false

    init {
        containerHeight = 166
    }

    override fun init() {
        super.init()
        isNarrow = width < 379
        recipeBookGui.initialize(width, height, minecraft!!, isNarrow, container)
        left = recipeBookGui.findLeftEdge(isNarrow, width, containerWidth)
        this.children.add(recipeBookGui)
        method_20085(recipeBookGui)
        addButton(
            RecipeBookButtonWidget(
                left + 5,
                height / 2 - 49,
                20, 18, 0, 0, 19,
                recipeButtonTex
            ) {
                it as RecipeBookButtonWidget
                recipeBookGui.reset(isNarrow)
                recipeBookGui.toggleOpen()
                left = recipeBookGui.findLeftEdge(isNarrow, width, containerWidth)
                it.setPos(left + 5, height / 2 - 49)
            }
        )
    }

    override fun tick() {
        super.tick()
        recipeBookGui.update()
    }

    override fun render(mouseX: Int, mouseY: Int, lastFrameDuration: Float) {
        this.renderBackground()

        if (recipeBookGui.isOpen && isNarrow) {
            drawBackground(lastFrameDuration, mouseX, mouseY)
            recipeBookGui.render(mouseX, mouseY, lastFrameDuration)
        } else {
            recipeBookGui.render(mouseX, mouseY, lastFrameDuration)
            super.render(mouseX, mouseY, lastFrameDuration)
            recipeBookGui.drawGhostSlots(left, top, true, lastFrameDuration)
        }

        drawMouseoverTooltip(mouseX, mouseY)
        recipeBookGui.drawTooltip(left, top, mouseX, mouseY)
        method_20086(recipeBookGui)
    }

    override fun drawForeground(mouseX: Int, mouseY: Int) {
        font.draw(title.formattedText, 28.0f, 6.0f, 4210752)
        font.draw(playerInventory.displayName.formattedText, 8.0f, (containerHeight - 96 + 2).toFloat(), 4210752)
    }

    override fun drawBackground(lastFrameDuration: Float, mouseX: Int, mouseY: Int) {
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        minecraft!!.textureManager.bindTexture(bgTex)
        val left = left
        val top = (height - containerHeight) / 2
        this.blit(left, top, 0, 0, containerWidth, containerHeight)
    }

    override fun isPointWithinBounds(boundX: Int, boundY: Int, width: Int, height: Int, pointX: Double, pointY: Double) =
        (!isNarrow || !recipeBookGui.isOpen)
            && super.isPointWithinBounds(boundX, boundY, width, height, pointX, pointY)

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int) =
        recipeBookGui.mouseClicked(mouseX, mouseY, button)
            || (isNarrow && recipeBookGui.isOpen)
            || super.mouseClicked(mouseX, mouseY, button)

    override fun isClickOutsideBounds(x: Double, y: Double, left: Int, top: Int, button: Int) =
        recipeBookGui.isClickOutsideBounds(
            x, y,
            this.left, this.top,
            containerWidth, containerHeight,
            button
        ) && (x < left.toDouble()
            || y < top.toDouble()
            || x >= (left + containerWidth).toDouble()
            || y >= (top + containerHeight).toDouble())

    override fun onMouseClick(slot: Slot?, mouseX: Int, mouseY: Int, actionType: SlotActionType) {
        super.onMouseClick(slot, mouseX, mouseY, actionType)
        recipeBookGui.slotClicked(slot)
    }

    override fun refreshRecipeBook() = recipeBookGui.refresh()

    override fun removed() {
        recipeBookGui.close()
        super.removed()
    }

    override fun getRecipeBookGui() = recipeBookGui
}
