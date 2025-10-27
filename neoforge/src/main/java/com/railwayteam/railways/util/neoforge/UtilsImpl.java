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

package com.railwayteam.railways.util.neoforge;

import com.railwayteam.railways.Railways;
import com.simibubi.create.content.trains.entity.Train;
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

    public static void sendCreatePacketToServer(Object packet) {
        // Unused in current codebase; reserved for future Create packet forwarding if needed
        Railways.LOGGER.warn("sendCreatePacketToServer not implemented for 1.21.1 (Create networking API pending)");
    }

    public static void sendHonkPacket(Train train, boolean isHonk) {
        // Used in ConductorEntity for train horn control during possession
        try {
            // send a simple C2S packet handled by our mod which will invoke Create's train honk on the server
            java.util.UUID id = train.id;
            com.railwayteam.railways.util.packet.HonkTrainPacket packet = new com.railwayteam.railways.util.packet.HonkTrainPacket(id, isHonk);
            com.railwayteam.railways.registry.CRPackets.PACKETS.send(packet);
        } catch (Throwable t) {
            Railways.LOGGER.warn("sendHonkPacket: failed to send honk packet (falling back to no-op): {}", t.toString());
        }
    }

    public static Path modsDir() {
		return FMLPaths.MODSDIR.get();
    }
}
