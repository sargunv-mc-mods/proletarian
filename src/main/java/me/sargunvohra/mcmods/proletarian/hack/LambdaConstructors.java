package me.sargunvohra.mcmods.proletarian.hack;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.Dynamic;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Timestamp;
import net.minecraft.util.registry.Registry;

import java.util.Optional;
import java.util.function.Function;

//to create various impl's of functional interfaces made thorugh lambdas, since I can't figure out how to do that in Kotlin
public class LambdaConstructors {
    //to prevent ClassCastExceptions, since you can't easily create a java Function from Kotlin
    public static final Optional<Function<Dynamic<?>, Timestamp>> FED_MEMORY_MODULE = Optional.of(Timestamp::of);

    public static final SuggestionProvider<ServerCommandSource> VILLAGER_TYPE_SUGGESTIONS = (context, builder) -> CommandSource.suggestIdentifiers(Registry.VILLAGER_TYPE.getIds(), builder);
}
