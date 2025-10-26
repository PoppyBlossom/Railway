/*
 * Compile-time stub for Create's Carriage to allow mixins and Railways code to compile.
 * The real implementation is provided by the Create mod at runtime.
 */
package com.simibubi.create.content.trains.entity;

import com.simibubi.create.content.contraptions.MountedStorage;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.createmod.catnip.data.Couple;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Carriage {
    // Reference to the train this carriage belongs to
    public Train train;
    
    // Carriage ID
    public int id;
    
    // Bogey configuration - single or double bogey
    @Nullable
    protected CarriageBogey leadingBogey;
    @Nullable
    protected CarriageBogey trailingBogey;
    protected int bogeySpacing;
    
    // Couple of both bogeys for easy iteration
    public Couple<CarriageBogey> bogeys;
    
    // Entities representing this carriage in different dimensions
    protected List<CarriageContraptionEntity> carriageEntities = new ArrayList<>();
    
    // Conductor presence indicators
    public Couple<Boolean> presentConductors = Couple.create(false, false);
    
    // Storage mounted on this carriage
    public MountedStorage storage;
    
    /**
     * Create 1.21.1 constructor: (leading bogey, trailing bogey or null, spacing)
     * For single-bogey carriages (like handcars), trailingBogey is null and spacing is 0.
     */
    public Carriage(@Nullable CarriageBogey leadingBogey, @Nullable CarriageBogey trailingBogey, int bogeySpacing) {
        this.leadingBogey = leadingBogey;
        this.trailingBogey = trailingBogey;
        this.bogeySpacing = bogeySpacing;
        this.storage = new MountedStorage();
        this.bogeys = Couple.create(leadingBogey, trailingBogey);
        this.id = 0; // Stub default
    }
    
    public void setTrain(Train train) {
        this.train = train;
    }
    
    public Train getTrain() {
        return train;
    }
    
    @Nullable
    public CarriageBogey leadingBogey() {
        return leadingBogey;
    }
    
    @Nullable
    public CarriageBogey trailingBogey() {
        return trailingBogey;
    }
    
    public int getBogeySpacing() {
        return bogeySpacing;
    }
    
    /**
     * Returns the leading or trailing bogey based on the parameter.
     */
    @Nullable
    public CarriageBogey bogey(boolean leading) {
        return leading ? leadingBogey : trailingBogey;
    }
    
    /**
     * Gets the leading travelling point.
     */
    public TravellingPoint getLeadingPoint() {
        return leadingBogey != null ? leadingBogey.leading() : null;
    }
    
    /**
     * Gets the trailing travelling point.
     */
    public TravellingPoint getTrailingPoint() {
        if (trailingBogey != null)
            return trailingBogey.trailing();
        if (leadingBogey != null)
            return leadingBogey.trailing();
        return null;
    }
    
    /**
     * Returns any available carriage entity, preferring the first one found.
     */
    @Nullable
    public CarriageContraptionEntity anyAvailableEntity() {
        return carriageEntities.isEmpty() ? null : carriageEntities.get(0);
    }
    
    /**
     * Iterates over all present carriage entities.
     */
    public void forEachPresentEntity(Consumer<CarriageContraptionEntity> consumer) {
        for (CarriageContraptionEntity entity : carriageEntities) {
            if (entity != null && entity.isAlive()) {
                consumer.accept(entity);
            }
        }
    }
    
    /**
     * Add a carriage entity to this carriage's entity list.
     */
    public void addEntity(CarriageContraptionEntity entity) {
        if (!carriageEntities.contains(entity)) {
            carriageEntities.add(entity);
        }
    }
    
    /**
     * Remove a carriage entity from this carriage's entity list.
     */
    public void removeEntity(CarriageContraptionEntity entity) {
        carriageEntities.remove(entity);
    }
    
    /**
     * Clear all carriage entities.
     */
    public void clearEntities() {
        carriageEntities.clear();
    }
    
    /**
     * Travel along the track by a certain distance.
     * 
     * @param level The world level
     * @param graph The track graph
     * @param distance The distance to travel (can be negative)
     * @param toFollowForward Optional point to follow when moving forward
     * @param toFollowBackward Optional point to follow when moving backward
     * @param type Travel type flags
     * @return The actual distance travelled
     */
    public double travel(Level level, TrackGraph graph, double distance, 
                         TravellingPoint toFollowForward, TravellingPoint toFollowBackward, int type) {
        // Stub: actual implementation moves the bogeys along the track
        // and updates carriage entity positions
        return 0.0;
    }
    
    /**
     * Update conductor presence flags.
     */
    public void updateConductors(Level level) {
        // Stub: checks for conductor entities in carriage contraption seats
    }
    
    /**
     * Check if the carriage is on an incompatible track type.
     */
    public boolean isOnIncompatibleTrack() {
        // Stub: validates track material compatibility with bogey types
        return false;
    }
    
    /**
     * Serialize carriage to NBT.
     */
    public CompoundTag write(DimensionPalette dimensions) {
        CompoundTag tag = new CompoundTag();
        
        if (leadingBogey != null) {
            tag.put("LeadingBogey", leadingBogey.write(dimensions));
        }
        
        if (trailingBogey != null) {
            tag.put("TrailingBogey", trailingBogey.write(dimensions));
        }
        
        tag.putInt("BogeySpacing", bogeySpacing);
        
        if (storage != null) {
            tag.put("Storage", storage.write());
        }
        
        return tag;
    }
    
    /**
     * Deserialize carriage from NBT.
     */
    public static Carriage read(CompoundTag tag, TrackGraph graph, DimensionPalette dimensions) {
        // Stub: actual implementation reconstructs carriage from saved data
        return null;
    }
    
    /**
     * Manage carriage entities (spawn/despawn based on loaded chunks).
     */
    public void manageEntities(Level level) {
        // Stub: handles entity lifecycle based on chunk loading state
    }
    
    /**
     * Set the contraption for this carriage and spawn entities.
     */
    public void setContraption(Level level, CarriageContraption contraption) {
        // Stub: assembles the contraption and spawns carriage entities
    }
    
    /**
     * Checks if a carriage has a single bogey (like handcars).
     */
    public boolean isSingleBogey() {
        return trailingBogey == null;
    }
    
    /**
     * Gets the index of this carriage in the train.
     */
    public int index() {
        if (train == null)
            return -1;
        return train.carriages.indexOf(this);
    }
    
    /**
     * Checks if carriage is on two bogeys (not handcar).
     */
    public boolean isOnTwoBogeys() {
        return trailingBogey != null;
    }
    
    /**
     * Get dimensional carriage entity for a specific level.
     */
    public DimensionalCarriageEntity getDimensional(Level level) {
        // Stub: returns wrapper with entity for this dimension
        return new DimensionalCarriageEntity();
    }
    
    /**
     * Inner class for dimension-specific carriage entity wrapper.
     */
    public static class DimensionalCarriageEntity {
        public java.util.Optional<CarriageContraptionEntity> entity = java.util.Optional.empty();
    }
}
