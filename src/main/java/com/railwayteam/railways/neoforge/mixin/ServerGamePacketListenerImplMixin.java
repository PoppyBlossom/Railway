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

package com.railwayteam.railways.neoforge.mixin;

import com.railwayteam.railways.multiloader.PacketSet;
import com.railwayteam.railways.multiloader.neoforge.PacketSetImpl;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	/*
	 * 1.21.x note:
	 * The per-player connection class still exposes a 'player' field; targeting the subclass
	 * ensures we can @Shadow it safely. The base ServerCommonPacketListenerImpl lacks this field
	 * and caused a mixin failure. We inject at HEAD of handleCustomPayload to intercept our wrapper
	 * before vanilla dispatch. This preserves player context without brittle reflection.
	 */
	@Shadow
	public ServerPlayer player;

	@Inject(
			method = "handleCustomPayload",
			at = @At("HEAD"),
			cancellable = true
	)
	private void railways$handleC2S(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
		var payload = packet.payload();
		if (payload instanceof com.railwayteam.railways.multiloader.neoforge.CustomPayloadWrapper wrapper) {
			PacketSet handler = PacketSetImpl.HANDLERS.get(wrapper.id());
			if (handler != null) {
				handler.handleC2SPacket(player, wrapper.data());
				ci.cancel();
			}
		}
	}
}
