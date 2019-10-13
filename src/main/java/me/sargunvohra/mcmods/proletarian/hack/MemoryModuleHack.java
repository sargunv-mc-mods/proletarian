package me.sargunvohra.mcmods.proletarian.hack;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.Timestamp;

import java.util.Optional;
import java.util.function.Function;

public class MemoryModuleHack {
    //to prevent ClassCastExceptions, since you can't easily create a java Function from Kotlin
    public static final Optional<Function<Dynamic<?>, Timestamp>> FED_MEMORY_MODULE = Optional.of(Timestamp::of);
}
