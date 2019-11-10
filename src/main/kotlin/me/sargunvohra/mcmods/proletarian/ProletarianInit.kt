package me.sargunvohra.mcmods.proletarian

import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationInit
import me.sargunvohra.mcmods.proletarian.jobboard.JobBoardInit
import me.sargunvohra.mcmods.proletarian.profession.CustomProfessionInit
import net.fabricmc.api.ModInitializer

@Suppress("unused")
object ProletarianInit : ModInitializer {
    override fun onInitialize() {
        CraftingStationInit.register()
        JobBoardInit.register()
        CustomProfessionInit.register()
    }
}
