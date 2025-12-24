/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * Proper 1.21.1 adaptation of the former ChunkMapMixin: replaces shadowing of a removed
 * method signature with a stable injection using the updated ChunkTrackingView API.
 */
package com.railwayteam.railways.mixin.conductor_possession;

import com.railwayteam.railways.content.conductor.ConductorPossessionController;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkTrackingView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restores conductor camera chunk tracking without relying on the removed
 * updateChunkTracking(ServerPlayer, ChunkPos, MutableObject, boolean, boolean) signature.
 *
 * Strategy:
 *  - Intercept early in updateChunkTracking(ServerPlayer)
 *  - If player is possessing a ConductorEntity, re-center the ChunkTrackingView
 *    on the camera entity's chunk position while preserving view distance.
 *  - Cancel the original method to avoid redundant recalculation using player's own position.
 *
 * This uses only stable named methods present in 1.21.1: getPlayerViewDistance(ServerPlayer) and applyChunkTrackingView.
 */
@Mixin(ChunkMap.class)
public abstract class CameraChunkTrackingMixin {

    @Shadow
    protected abstract int getPlayerViewDistance(ServerPlayer player);

    @Shadow
    protected abstract void applyChunkTrackingView(ServerPlayer player, ChunkTrackingView view);

    @Inject(method = "updateChunkTracking", at = @At("HEAD"), cancellable = true)
    private void railways$redirectToCamera(ServerPlayer player, CallbackInfo ci) {
        if (ConductorPossessionController.isPossessingConductor(player)) {
            // Center on the camera entity rather than the player itself.
            var camera = player.getCamera();
            if (camera == null) return; // Safety check for null camera
            ChunkPos cameraChunk = camera.chunkPosition();
            int dist = this.getPlayerViewDistance(player);

            // Avoid unnecessary updates if already centered correctly.
            if (!(player.getChunkTrackingView() instanceof ChunkTrackingView.Positioned positioned
                    && positioned.center().equals(cameraChunk)
                    && positioned.viewDistance() == dist)) {
                this.applyChunkTrackingView(player, ChunkTrackingView.of(cameraChunk, dist));
            }
            ci.cancel();
        }
    }
}
