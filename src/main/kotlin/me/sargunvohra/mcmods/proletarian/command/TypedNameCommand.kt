package me.sargunvohra.mcmods.proletarian.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import me.sargunvohra.mcmods.proletarian.name.VillagerNamer
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class TypedNameCommand: Command<ServerCommandSource> {
        override fun run(context: CommandContext<ServerCommandSource>?): Int {
            val id = context!!.getArgument("type", Identifier::class.java)
            if (Registry.VILLAGER_TYPE.ids.contains(id)) {
                val type = Registry.VILLAGER_TYPE.get(id)
                val name = VillagerNamer.getFullName(type)
                context.source.sendFeedback(LiteralText(name.left.plus(" ").plus(name.right)), false)
                return 1
            } else {
                context.source.sendError(LiteralText("Could not find villager type with ID ".plus(id.toString())))
                return 0
            }
        }
}
