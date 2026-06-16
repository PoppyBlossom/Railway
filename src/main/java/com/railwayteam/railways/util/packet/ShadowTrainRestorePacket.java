/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.railwayteam.railways.mixin.AccessorTrainRelocator;
import com.railwayteam.railways.multiloader.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * Packet sent from server to client to initiate shadow train restoration.
 * Uses the real train UUID directly in relocatingTrain — no MARKER needed.
 */
public class ShadowTrainRestorePacket implements S2CPacket {
    private final UUID trainId;

    public ShadowTrainRestorePacket(UUID trainId) {
        this.trainId = trainId;
    }

    public ShadowTrainRestorePacket(FriendlyByteBuf buf) {
        this.trainId = buf.readUUID();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(trainId);
    }

    @Override
    public void handle(Minecraft mc) {
        mc.execute(() -> {
            if (mc.player == null) return;
            if (trainId == null) return;

            // Try to find the train in the client graph
            com.simibubi.create.content.trains.entity.Train train =
                com.simibubi.create.CreateClient.RAILWAYS.trains.get(trainId);
            if (train != null) {
                ShadowRealm.clientShadowRestoringTrain = train;
            }
            ShadowRealm.clientShadowRestoringTrainId = trainId;

            // Use the real train ID directly — no MARKER indirection
            AccessorTrainRelocator.railways$setRelocatingTrain(trainId);
            AccessorTrainRelocator.railways$setRelocatingOrigin(BlockPos.containing(mc.player.position()));
            AccessorTrainRelocator.railways$setRelocatingEntityId(-1);
        });
    }
}
