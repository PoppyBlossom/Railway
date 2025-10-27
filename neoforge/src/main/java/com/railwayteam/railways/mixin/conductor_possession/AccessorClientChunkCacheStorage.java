package com.railwayteam.railways.mixin.conductor_possession;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for ClientChunkCache.Storage to get/set view center fields without reflection.
 * We target the class by name to avoid compile-time visibility issues.
 */
@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public interface AccessorClientChunkCacheStorage {
    @Accessor("viewCenterX")
    int railways$getViewCenterX();

    @Accessor("viewCenterX")
    void railways$setViewCenterX(int value);

    @Accessor("viewCenterZ")
    int railways$getViewCenterZ();

    @Accessor("viewCenterZ")
    void railways$setViewCenterZ(int value);
}
