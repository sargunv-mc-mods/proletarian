package me.sargunvohra.mcmods.proletarian.command

import me.sargunvohra.mcmods.proletarian.hack.LambdaConstructors
import net.fabricmc.fabric.api.registry.CommandRegistry
import net.minecraft.command.arguments.IdentifierArgumentType
import net.minecraft.server.command.CommandManager

object CommandInit {

    fun register() {
        CommandRegistry.INSTANCE.register(false) { dispatcher ->
            val rootNode = CommandManager
                .literal("proletarian")
                .build()

            val nameNode = CommandManager
                .literal("name")
                .executes(NameCommand())
                .build()

            val typedNameNode = CommandManager
                .argument("type", IdentifierArgumentType.identifier())
                .suggests(LambdaConstructors.VILLAGER_TYPE_SUGGESTIONS)
                .executes(TypedNameCommand())
                .build()

            nameNode.addChild(typedNameNode)
            rootNode.addChild(nameNode)
            dispatcher.root.addChild(rootNode)
        }
    }
}
