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

package com.railwayteam.railways.multiloader;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

/**
 * Find players to send S2C packets to.
 */
public abstract class PlayerSelection {
	public abstract void accept(ResourceLocation id, FriendlyByteBuf buffer);	public static PlayerSelection all() {
    return com.railwayteam.railways.multiloader.neoforge.PlayerSelectionImpl.all();
	}	public static PlayerSelection allWith(Predicate<ServerPlayer> condition) {
    return com.railwayteam.railways.multiloader.neoforge.PlayerSelectionImpl.allWith(condition);
	}	public static PlayerSelection of(ServerPlayer player) {
    return com.railwayteam.railways.multiloader.neoforge.PlayerSelectionImpl.of(player);
	}	public static PlayerSelection tracking(Entity entity) {
    return com.railwayteam.railways.multiloader.neoforge.PlayerSelectionImpl.tracking(entity);
	}	public static PlayerSelection trackingWith(Entity entity, Predicate<ServerPlayer> condition) {
    return com.railwayteam.railways.multiloader.neoforge.PlayerSelectionImpl.trackingWith(entity, condition);
	}	public static PlayerSelection tracking(BlockEntity be) {
    return com.railwayteam.railways.multiloader.neoforge.PlayerSelectionImpl.tracking(be);
	}	public static PlayerSelection tracking(ServerLevel level, BlockPos pos) {
    return com.railwayteam.railways.multiloader.neoforge.PlayerSelectionImpl.tracking(level, pos);
	}	public static PlayerSelection trackingAndSelf(ServerPlayer player) {
    return com.railwayteam.railways.multiloader.neoforge.PlayerSelectionImpl.trackingAndSelf(player);
	}
}
