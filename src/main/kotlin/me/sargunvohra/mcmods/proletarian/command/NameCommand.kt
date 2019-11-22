package me.sargunvohra.mcmods.proletarian.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import me.sargunvohra.mcmods.proletarian.name.VillagerNamer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText

class NameCommand: Command<ServerCommandSource> {
    override fun run(context: CommandContext<ServerCommandSource>?): Int {
        val name = VillagerNamer.getFullName()
        context!!.source.sendFeedback(LiteralText(name.left + " " + name.right), false)
        return 1
    }
}

