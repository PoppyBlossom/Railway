package com.simibubi.create.content.contraptions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Stub class for Create's Contraption.
 * This is a compile-time placeholder - Create's actual implementation will be used at runtime.
 */
public class Contraption {
    
    // Block entities present in the contraption
    public Map<BlockPos, BlockEntity> presentBlockEntities = new HashMap<>();
    
    // Entity reference for contraption (used in some contexts)
    public AbstractContraptionEntity entity;
    
    public Contraption() {
        // Stub constructor
    }
    
    public Direction getAssemblyDirection() {
        return Direction.NORTH; // Stub return
    }
    
    public void disassemble(Level level) {
        // Runtime implementation by Create
    }
    
    public Map<BlockPos, ? extends BlockEntity> getSpecialRenderedBlockEntities() {
        return presentBlockEntities; // Stub return
    }
    
    public Map<BlockPos, StructureTemplate.StructureBlockInfo> getBlocks() {
        return new HashMap<>(); // Stub return
    }
    
    public MountedStorage getStorage() {
        return new MountedStorage(); // Stub return
    }
    
    public void addPassengersToWorld(Level level, Object transform, Object passengers) {
        // Runtime implementation by Create
    }
}
