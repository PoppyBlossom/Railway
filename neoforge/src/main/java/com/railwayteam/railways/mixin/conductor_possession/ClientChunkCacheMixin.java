/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.mixin.conductor_possession;

import com.railwayteam.railways.content.conductor.ClientHandler;
import com.railwayteam.railways.content.conductor.ConductorPossessionController;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

/**
 * These mixins aim at implementing the camera chunk storage from CameraController into all the places
 * ClientChunkCache#storage is used
 *
 * Confirmed working with Security Craft
 */
@Mixin(value = ClientChunkCache.class, priority = 1200)
public abstract class ClientChunkCacheMixin {
	@Shadow
	@Final
	ClientLevel level;

	private Object newStorage(int viewDistance) {
		try {
			if ((Object) this instanceof ClientChunkCache cache) {
				Class<?> storageClass = Class.forName("net.minecraft.client.multiplayer.ClientChunkCache$Storage");
				java.lang.reflect.Constructor<?> ctor = storageClass.getDeclaredConstructor(ClientChunkCache.class, int.class);
				ctor.setAccessible(true);
				return ctor.newInstance(cache, viewDistance);
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	@Shadow
	private static boolean isValidChunk(LevelChunk chunk, int x, int z) {
		throw new IllegalStateException("Shadowing isValidChunk did not work!");
	}

	/**
	 * Initializes the camera storage
	 */
	@Inject(method = "<init>", at = @At(value = "TAIL"))
	public void railways$securitycraft$onInit(ClientLevel level, int viewDistance, CallbackInfo ci) {
		ConductorPossessionController.setCameraStorage(newStorage(Math.max(2, viewDistance) + 3));
	}

	/**
	 * Updates the camera storage's view radius by creating a new Storage instance with the same view center and chunks as the
	 * previous one
	 */
	@Inject(method = "updateViewRadius", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;<init>(Lnet/minecraft/client/multiplayer/ClientChunkCache;I)V"))
	public void railways$securitycraft$onUpdateViewRadius(int viewDistance, CallbackInfo ci) {
		// For 1.21, avoid directly accessing private Storage internals; just swap to a fresh storage instance
		Object storage = newStorage(Math.max(2, viewDistance) + 3);
		if (storage != null)
			ConductorPossessionController.setCameraStorage(storage);
	}

	/**
	 * Handles chunks that are dropped in range of the camera storage
	 * 1.21.1 note: Method signature changed from (int x, int z) to (ChunkPos pos)
	 */
	@Inject(method = "drop", at = @At(value = "HEAD"))
	public void railways$securitycraft$onDrop(net.minecraft.world.level.ChunkPos pos, CallbackInfo ci) {
		if (ClientHandler.isPlayerMountedOnCamera()) {
			// Disabled for 1.21 migration: camera chunk drop handling uses private Storage internals
			return;
		}
	}

	/**
	 * Handles chunks that get sent to the client which are in range of the camera storage, i.e. place them into the storage for
	 * them to be acquired afterwards
	 */
	@Inject(method = "replaceWithPacketData", at = @At(value = "HEAD"), cancellable = true)
	private void railways$securitycraft$onReplace(int x, int z, FriendlyByteBuf buffer, CompoundTag chunkTag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> tagOutputConsumer, CallbackInfoReturnable<LevelChunk> callback) {
		if (ClientHandler.isPlayerMountedOnCamera()) {
			// Disabled for 1.21 migration; rely on default client chunk handling
			return;
		}
	}

	/**
	 * If chunks in range of a camera storage need to be acquired, ask the camera storage about these chunks
	 */
	@Inject(method = "getChunk(IILnet/minecraft/world/level/chunk/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;",
			slice = @Slice(from = @At(value = "RETURN", ordinal = 1)),
			at = @At("RETURN"), cancellable = true)
	private void railways$securitycraft$onGetChunk(int x, int z, ChunkStatus requiredStatus, boolean load, CallbackInfoReturnable<LevelChunk> callback) {
		if (ClientHandler.isPlayerMountedOnCamera()) {
			// Disabled for 1.21 migration; rely on default client chunk access path
		}
	}
}
