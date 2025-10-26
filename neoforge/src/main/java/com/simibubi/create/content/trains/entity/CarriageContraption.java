package com.simibubi.create.content.trains.entity;

import com.simibubi.create.content.contraptions.Contraption;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stub class for Create's CarriageContraption.
 * This is a compile-time placeholder - Create's actual implementation will be used at runtime.
 */
public class CarriageContraption extends Contraption {
    
    public CarriageContraptionEntity entity;
    public Map<BlockPos, Couple<Boolean>> conductorSeats = new HashMap<>();
    
    public CarriageContraption() {
        super();
    }
    
    // Stub methods matching Create API surface
    public void assemble(Level level) {
        // Runtime implementation by Create
    }
    
    public boolean hasBackwardControls() {
        return false; // Stub return
    }
    
    public BlockPos getSeatOf(UUID uuid) {
        return BlockPos.ZERO; // Stub return
    }
    
    public void addPassengersToWorld(Level level, Vec3 transform, Object passengers) {
        // Runtime implementation by Create
    }
}
