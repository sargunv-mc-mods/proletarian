package me.sargunvohra.mcmods.proletarian

import me.sargunvohra.mcmods.proletarian.command.CommandInit
import me.sargunvohra.mcmods.proletarian.craftingstation.CraftingStationInit
import me.sargunvohra.mcmods.proletarian.jobboard.JobBoardInit
import me.sargunvohra.mcmods.proletarian.name.VillagerNamer
import me.sargunvohra.mcmods.proletarian.network.ProletarianNetworking
import me.sargunvohra.mcmods.proletarian.profession.CustomProfessionInit
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resource.ResourceType

@Suppress("unused")
object ProletarianInit : ModInitializer {
    override fun onInitialize() {
        CraftingStationInit.register()
        JobBoardInit.register()
        CustomProfessionInit.register()
        CommandInit.register()
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(VillagerNamer()) //TODO: create an init object?
        ProletarianNetworking.registerCommon()
    }
}
