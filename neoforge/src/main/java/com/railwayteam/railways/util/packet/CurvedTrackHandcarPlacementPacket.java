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

package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.multiloader.C2SPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.simibubi.create.content.trains.track.TrackBlockEntity;

public class CurvedTrackHandcarPlacementPacket implements C2SPacket {

    private final BlockPos pos;
    private final BlockPos targetPos;
    private final boolean front;
    private final int segment;
    private final int slot;

    public CurvedTrackHandcarPlacementPacket(BlockPos pos, BlockPos targetPos, int segment, boolean front, int slot) {
        this.pos = pos;
        this.targetPos = targetPos;
        this.segment = segment;
        this.front = front;
        this.slot = slot;
    }

    public CurvedTrackHandcarPlacementPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        targetPos = buf.readBlockPos();
        segment = buf.readVarInt();
        front = buf.readBoolean();
        slot = buf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBlockPos(targetPos);
        buffer.writeVarInt(segment);
        buffer.writeBoolean(front);
        buffer.writeVarInt(slot);
    }

    protected void actuallyHandle(ServerPlayer player, TrackBlockEntity be) {
        // TODO 1.21 port: Handcar train creation is disabled; this packet body requires rework
        Railways.LOGGER.warn("CurvedTrackHandcarPlacementPacket.actuallyHandle temporarily disabled for 1.21 port");
    }

    @Override
    public void handle(ServerPlayer sender) {
        Level world = sender.level();
        if (world == null || !world.isLoaded(pos))
            return;
        if (!pos.closerThan(sender.blockPosition(), 64))
            return;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof TrackBlockEntity tbe) {
            actuallyHandle(sender, tbe);
        }
    }
}
