package com.simibubi.create.content.trains.entity;

import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;

/**
 * Stub class for compilation only.
 * The actual TrainPacket is provided by the Create mod at runtime.
 * This stub provides the minimal structure needed for Railways mixins.
 */
public class TrainPacket {
    public Train train;
    public UUID trainId;
    
    public TrainPacket(Train train, boolean add) {
        // stub constructor
    }
    
    public TrainPacket(FriendlyByteBuf buffer) {
        // stub constructor for deserialization
    }
    
    public void write(FriendlyByteBuf buffer) {
        // stub method
    }
    
    public void handle() {
        // stub method
    }
}
