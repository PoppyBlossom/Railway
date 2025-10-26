package com.simibubi.create.content.trains.entity;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Stub class for Create's CarriageContraptionEntity.
 * This is a compile-time placeholder - Create's actual implementation will be used at runtime.
 */
public class CarriageContraptionEntity extends AbstractContraptionEntity {
    
    protected Carriage carriage;
    public UUID trainId;
    public int carriageIndex;
    
    public CarriageContraptionEntity(EntityType<?> type, Level level) {
        super(type, level);
    }
    
    public Carriage getCarriage() {
        return carriage;
    }
    
    public void setCarriage(Carriage carriage) {
        this.carriage = carriage;
    }
    
    public Optional<UUID> getControllingPlayer() {
        return Optional.empty(); // Stub return
    }
    
    public void control(BlockPos pos, Set<Integer> controls, ServerPlayer player) {
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
