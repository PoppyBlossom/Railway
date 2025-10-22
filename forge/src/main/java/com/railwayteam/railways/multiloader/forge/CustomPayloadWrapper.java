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

package com.railwayteam.railways.multiloader.forge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Wrapper for custom payload packets in Minecraft 1.21.1+
 * This wraps our packet data into the new CustomPacketPayload format.
 */
public record CustomPayloadWrapper(Type<CustomPayloadWrapper> type, FriendlyByteBuf data) implements CustomPacketPayload {
    
    public static CustomPayloadWrapper create(ResourceLocation id, FriendlyByteBuf data) {
        return new CustomPayloadWrapper(new Type<>(id), data);
    }
    
    public ResourceLocation id() {
        return type.id();
    }
    
    /**
     * Returns a {@link StreamCodec} for serializing and deserializing {@link CustomPayloadWrapper} instances.
     * <p>
     * This method is intended to be used for registering the codec with Minecraft's networking system
     * when custom payload packets are sent or received. If you require codec registration for your
     * custom payloads, use this method to obtain the appropriate codec and register it as needed.
     * <p>
     * If codec registration is not required, this method can be safely ignored.
     *
     * @param id The {@link ResourceLocation} identifier for the custom payload type.
     * @return A {@link StreamCodec} for {@link CustomPayloadWrapper}.
     */
    public static StreamCodec<FriendlyByteBuf, CustomPayloadWrapper> codec(ResourceLocation id) {
        return StreamCodec.of(
            (buf, payload) -> buf.writeBytes(payload.data),
            (buf) -> {
                FriendlyByteBuf data = new FriendlyByteBuf(buf.readRetainedSlice(buf.readableBytes()));
                return new CustomPayloadWrapper(new Type<>(id), data);
            }
        );
    }
}
