package me.sargunvohra.mcmods.proletarian

import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationInit
import me.sargunvohra.mcmods.proletarian.network.ProletarianNetworking
import net.fabricmc.api.ClientModInitializer

@Suppress("unused")
object ProletarianClientInit : ClientModInitializer {
    override fun onInitializeClient() {
        CraftingStationInit.registerClient()
        ProletarianNetworking.registerClient()
    }
}
