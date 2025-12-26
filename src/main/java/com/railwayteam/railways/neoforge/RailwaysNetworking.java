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

package com.railwayteam.railways.neoforge;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.multiloader.neoforge.CustomPayloadWrapper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Handles registration of custom network payloads for Railways mod.
 * In NeoForge 1.21.1+, all custom payloads must be explicitly registered with the networking system.
 */
public class RailwaysNetworking {
    
    // Packet IDs used by Railways
    private static final ResourceLocation C2S_PACKET_ID = ResourceLocation.fromNamespaceAndPath(Railways.MOD_ID, "c2s");
    private static final ResourceLocation S2C_PACKET_ID = ResourceLocation.fromNamespaceAndPath(Railways.MOD_ID, "s2c");
    
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        Railways.LOGGER.info("Registering Railways network payloads...");
        
        final PayloadRegistrar registrar = event.registrar(Railways.MOD_ID)
            .versioned("1.0.0")
            .optional(); // Mark as optional so clients/servers without the mod can still connect
        
        // Register client-to-server payload
        registrar.playToServer(
            CustomPayloadWrapper.type(C2S_PACKET_ID),
            CustomPayloadWrapper.codec(C2S_PACKET_ID),
            (payload, context) -> {
                // Handler is managed by our mixin system (ServerGamePacketListenerImplMixin)
                // This registration just tells NeoForge the payload type is valid
            }
        );
        
        // Register server-to-client payload
        registrar.playToClient(
            CustomPayloadWrapper.type(S2C_PACKET_ID),
            CustomPayloadWrapper.codec(S2C_PACKET_ID),
            (payload, context) -> {
                // Delegate to our packet handling system directly
                context.enqueueWork(() -> {
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.level != null) {
                        com.railwayteam.railways.registry.CRPackets.PACKETS.handleS2CPacket(mc, payload.data());
                    }
                });
            }
        );
        
        Railways.LOGGER.info("Railways network payloads registered successfully");
    }
}
