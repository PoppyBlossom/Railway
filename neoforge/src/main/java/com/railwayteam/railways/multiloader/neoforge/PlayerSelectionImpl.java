/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.multiloader.neoforge;

import com.railwayteam.railways.neoforge.mixin.ChunkMapAccessor;
import com.railwayteam.railways.multiloader.PlayerSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class PlayerSelectionImpl extends PlayerSelection {

private final Consumer<ClientboundCustomPayloadPacket> sender;

private PlayerSelectionImpl(Consumer<ClientboundCustomPayloadPacket> sender) {
this.sender = sender;
}

@Override
public void accept(ResourceLocation id, FriendlyByteBuf buffer) {
CustomPayloadWrapper payload = CustomPayloadWrapper.create(id, buffer);
ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);
sender.accept(packet);
}

public static PlayerSelection all() {
return new PlayerSelectionImpl(packet -> {
for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
player.connection.send(packet);
}
});
}

public static PlayerSelection allWith(Predicate<ServerPlayer> condition) {
return new PlayerSelectionImpl(packet -> {
for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
if (condition.test(player)) {
player.connection.send(packet);
}
}
});
}

public static PlayerSelection of(ServerPlayer player) {
return new PlayerSelectionImpl(packet -> player.connection.send(packet));
}

	public static PlayerSelection tracking(Entity entity) {
		return new PlayerSelectionImpl(packet -> {
			ServerChunkCache manager = (ServerChunkCache) entity.level().getChunkSource();
			ChunkMap storage = manager.chunkMap;
			Object trackedEntity = ((ChunkMapAccessor)storage).getEntityMap().get(entity.getId());

			if (trackedEntity == null)
				return;

			for (ServerPlayerConnection connection : ((ChunkMapAccessor.TrackedEntityAccessor) trackedEntity).getSeenBy()) {
				connection.send(packet);
			}
		});
	}

	public static PlayerSelection trackingWith(Entity entity, Predicate<ServerPlayer> condition) {
		return new PlayerSelectionImpl(packet -> {
			ServerChunkCache manager = (ServerChunkCache) entity.level().getChunkSource();
			ChunkMap storage = manager.chunkMap;
			Object trackedEntity = ((ChunkMapAccessor)storage).getEntityMap().get(entity.getId());

			if (trackedEntity == null)
				return;

			for (ServerPlayerConnection connection : ((ChunkMapAccessor.TrackedEntityAccessor) trackedEntity).getSeenBy()) {
				if (condition.test(connection.getPlayer())) {
					connection.send(packet);
				}
			}
		});
	}public static PlayerSelection tracking(BlockEntity be) {
LevelChunk chunk = be.getLevel().getChunkAt(be.getBlockPos());
return new PlayerSelectionImpl(packet -> {
ServerChunkCache manager = (ServerChunkCache) be.getLevel().getChunkSource();
manager.chunkMap.getPlayers(chunk.getPos(), false).forEach(player -> player.connection.send(packet));
});
}

public static PlayerSelection tracking(ServerLevel level, BlockPos pos) {
LevelChunk chunk = level.getChunkAt(pos);
return new PlayerSelectionImpl(packet -> {
ServerChunkCache manager = (ServerChunkCache) level.getChunkSource();
manager.chunkMap.getPlayers(chunk.getPos(), false).forEach(player -> player.connection.send(packet));
});
}

	public static PlayerSelection trackingAndSelf(ServerPlayer player) {
		return new PlayerSelectionImpl(packet -> {
			player.connection.send(packet);
			// Also send to all tracking players
			ServerChunkCache manager = (ServerChunkCache) player.level().getChunkSource();
			ChunkMap storage = manager.chunkMap;
			Object trackedEntity = ((ChunkMapAccessor)storage).getEntityMap().get(player.getId());

			if (trackedEntity != null) {
				for (ServerPlayerConnection connection : ((ChunkMapAccessor.TrackedEntityAccessor) trackedEntity).getSeenBy()) {
					connection.send(packet);
				}
			}
		});
	}
}