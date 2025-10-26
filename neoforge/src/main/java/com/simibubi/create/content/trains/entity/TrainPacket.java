/*
 * Compile-time stub for Create's TrainPacket to allow mixins to compile.
 * The real implementation is provided by the Create mod at runtime.
 */
package com.simibubi.create.content.trains.entity;

import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

@SuppressWarnings("unused")
public class TrainPacket {
    public Train train;
    public UUID trainId;

    // Placeholder constructor used by Create when forming packets
    public TrainPacket(Train train, boolean something) {
        this.train = train;
        this.trainId = train != null ? train.id : null;
    }

    // Placeholder constructor used when reading from buffer
    public TrainPacket(FriendlyByteBuf buf) {
        // minimal read to satisfy mixin constructor target
        this.trainId = buf.readUUID();
    }

    // Placeholder write method - real Create implementation writes train state
    public void write(FriendlyByteBuf buf) {
        if (trainId != null) buf.writeUUID(trainId);
    }

    // Placeholder handle method - real Create processes packet on client/server
    public void handle() {
        // no-op stub
    }
}
