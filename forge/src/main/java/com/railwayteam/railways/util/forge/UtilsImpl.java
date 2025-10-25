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

package com.railwayteam.railways.util.forge;

import com.railwayteam.railways.Railways;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class UtilsImpl {
	public static Path configDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	public static boolean isDevEnv() {
		return !FMLLoader.isProduction();
	}

    public static void sendCreatePacketToServer(SimplePacketBase packet) {
        // TODO 1.21 port: AllPackets.getChannel() no longer exists; network API rework needed
        Railways.LOGGER.warn("sendCreatePacketToServer temporarily disabled for 1.21 port");
    }

    public static void sendHonkPacket(Train train, boolean isHonk) {
        // TODO 1.21 port: AllPackets.getChannel() and PacketDistributor.ALL no longer exist
        Railways.LOGGER.warn("sendHonkPacket temporarily disabled for 1.21 port");
    }

    public static void postChunkEventClient(LevelChunk chunk, boolean load) {
        // TODO 1.21 port: MinecraftForge.EVENT_BUS no longer exists; use NeoForge event bus
        Railways.LOGGER.warn("postChunkEventClient temporarily disabled for 1.21 port");
    }

    public static Path modsDir() {
		return FMLPaths.MODSDIR.get();
    }
}
