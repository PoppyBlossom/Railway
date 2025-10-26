package com.simibubi.create.content.contraptions;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

/**
 * Stub class for Create's AbstractContraptionEntity.
 * This is a compile-time placeholder - Create's actual implementation will be used at runtime.
 */
public abstract class AbstractContraptionEntity extends Entity {
    
    protected Contraption contraption;
    
    public AbstractContraptionEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public Contraption getContraption() {
        return contraption;
    }
    
    public void setContraption(Contraption contraption) {
        this.contraption = contraption;
    }
    
    public Vec3 toGlobalVector(Vec3 localVec, float partialTicks) {
        return localVec; // Stub return
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Runtime implementation by Create
    }
    
    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        // Runtime implementation by Create
    }
    
    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        // Runtime implementation by Create
    }
}
