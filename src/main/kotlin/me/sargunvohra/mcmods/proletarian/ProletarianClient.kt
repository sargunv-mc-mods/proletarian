package me.sargunvohra.mcmods.proletarian

import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationInit
import net.fabricmc.api.ClientModInitializer

object ProletarianClient : ClientModInitializer {
    override fun onInitializeClient() {
        CraftingStationInit.registerClient()
    }
}
