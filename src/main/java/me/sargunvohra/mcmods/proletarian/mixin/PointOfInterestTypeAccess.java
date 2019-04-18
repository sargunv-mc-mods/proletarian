package me.sargunvohra.mcmods.proletarian.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.village.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PointOfInterestType.class)
public interface PointOfInterestTypeAccess {

    @Accessor(value = "BLOCK_STATE_TO_POINT_OF_INTEREST_TYPE")
    static Map<BlockState, PointOfInterestType> getStatePoiMap() {
        throw new IllegalStateException();
    }
}
