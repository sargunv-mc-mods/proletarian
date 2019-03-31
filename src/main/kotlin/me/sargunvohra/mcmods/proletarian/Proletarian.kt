package me.sargunvohra.mcmods.proletarian

import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationInit
import me.sargunvohra.mcmods.proletarian.profession.CustomProfessionInit
import net.fabricmc.api.ModInitializer

object Proletarian : ModInitializer {

    override fun onInitialize() {
        CraftingStationInit.register()
        CustomProfessionInit.register()
    }

}
