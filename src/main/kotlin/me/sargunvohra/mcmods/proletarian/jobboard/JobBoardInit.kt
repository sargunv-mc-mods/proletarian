package me.sargunvohra.mcmods.proletarian.jobboard

import me.sargunvohra.mcmods.proletarian.id
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.registry.Registry

object JobBoardInit {
    private val ID = id("job_board")

    fun register() {
        Registry.register(Registry.BLOCK, ID, JobBoardBlock)

        Registry.register(
            Registry.ITEM, ID, BlockItem(JobBoardBlock, Item.Settings().group(ItemGroup.DECORATIONS)))
    }
}
