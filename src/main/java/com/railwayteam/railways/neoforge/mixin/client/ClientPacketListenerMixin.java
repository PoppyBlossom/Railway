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

package com.railwayteam.railways.neoforge.mixin.client;

import com.railwayteam.railways.multiloader.PacketSet;
import com.railwayteam.railways.multiloader.neoforge.PacketSetImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	/*
	 * 1.21.x note:
	 * Targeting ClientPacketListener (game-phase subclass) ensures we only intercept payloads
	 * during active gameplay, not during configuration handshake. This avoids interfering with
	 * NeoForge's own custom payloads (e.g., neoforge:custom_time_packet) that arrive before the
	 * client level is initialized. The method signature is handleCustomPayload(CustomPacketPayload)
	 * in the game phase, which differs from the base class packet-wrapping method.
	 */

	@Inject(
			method = "handleCustomPayload(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	private void railways$handleS2C(CustomPacketPayload payload, CallbackInfo ci) {
		// Only intercept our CustomPayloadWrapper packets; let all others (including NeoForge's) pass through
		if (payload instanceof com.railwayteam.railways.multiloader.neoforge.CustomPayloadWrapper wrapper) {
			PacketSet handler = PacketSetImpl.HANDLERS.get(wrapper.id());
			if (handler != null) {
				// Use the global Minecraft instance; level is guaranteed to exist in game phase
				handler.handleS2CPacket(Minecraft.getInstance(), wrapper.data());
				ci.cancel(); // Cancel only after successfully handling our packet
			}
			// If no handler registered for this wrapper ID, let it fall through (don't cancel)
		}
		// For all non-wrapper payloads, do nothing and let vanilla/NeoForge handle them
	}
}
