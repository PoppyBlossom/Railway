/*
 * Compile-time stub for Create's CarriageBogey to allow mixins and Railways code to compile.
 * The real implementation is provided by the Create mod at runtime.
 */
package com.simibubi.create.content.trains.entity;

import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.bogey.BogeyStyle;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.createmod.catnip.data.Couple;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class CarriageBogey {
    // NBT key for upside-down state
    public static final String UPSIDE_DOWN_KEY = "UpsideDown";
    
    // TravellingPoints representing the two wheel positions
    public TravellingPoint leading;
    public TravellingPoint trailing;
    
    // Bogey properties
    protected BogeySizes.BogeySize size;
    protected double spacing;
    protected boolean upsideDown;
    protected AbstractBogeyBlock<?> type;
    protected BogeyStyle style;
    
    // Coupling anchor positions (for visual coupling rendering)
    public Couple<Vec3> couplingAnchors = Couple.create(null, null);
    
    // Positions for bogey rendering
    public Couple<Vec3> positions = Couple.create(Vec3.ZERO, Vec3.ZERO);
    public Couple<Float> angles = Couple.create(0f, 0f);
    
    /**
     * Create 1.21.1 constructor pattern for CarriageBogey.
     * 
     * @param leading The leading TravellingPoint (front wheel position)
     * @param trailing The trailing TravellingPoint (back wheel position)
     * @param size The bogey size (SMALL or LARGE)
     * @param spacing The spacing between the two points
     * @param upsideDown Whether the bogey is mounted upside down
     * @param type The bogey block type
     */
    public CarriageBogey(TravellingPoint leading, TravellingPoint trailing, 
                         BogeySizes.BogeySize size, double spacing, 
                         boolean upsideDown, AbstractBogeyBlock<?> type) {
        this.leading = leading;
        this.trailing = trailing;
        this.size = size;
        this.spacing = spacing;
        this.upsideDown = upsideDown;
        this.type = type;
        this.style = type != null ? type.getDefaultStyle() : null;
    }
    
    public BogeyStyle getStyle() {
        return style;
    }
    
    public void setStyle(BogeyStyle style) {
        this.style = style;
    }
    
    @Nullable
    public AbstractBogeyBlock<?> getType() {
        return type;
    }
    
    public BogeySizes.BogeySize getSize() {
        return size;
    }
    
    public double getSpacing() {
        return spacing;
    }
    
    public boolean isUpsideDown() {
        return upsideDown;
    }
    
    public TravellingPoint leading() {
        return leading;
    }
    
    public TravellingPoint trailing() {
        return trailing;
    }
    
    /**
     * Returns the appropriate TravellingPoint based on direction.
     * @param leading true for leading point, false for trailing
     */
    public TravellingPoint getPoint(boolean leading) {
        return leading ? this.leading : this.trailing;
    }
    
    /**
     * Update coupling anchor positions for rendering.
     */
    public void updateCouplingAnchor(Vec3 entityPos, float entityXRot, float entityYRot, 
                                      int bogeySpacing, float partialTicks, boolean leading) {
        // Stub: actual implementation calculates anchor positions based on bogey geometry
        // This is overridden by mixins for special cases (invisible bogeys, etc.)
    }
    
    /**
     * Write bogey data to NBT for persistence.
     */
    public CompoundTag write(DimensionPalette dimensions) {
        CompoundTag tag = new CompoundTag();
        
        if (type != null) {
            // Note: Registry name retrieval is handled at runtime by Create
            tag.putString("Type", "stub:bogey");
        }
        
        if (style != null) {
            tag.putString("Style", style.id.toString());
        }
        
        tag.put("Leading", leading.write(dimensions));
        tag.put("Trailing", trailing.write(dimensions));
        tag.putDouble("Spacing", spacing);
        tag.putBoolean("UpsideDown", upsideDown);
        
        return tag;
    }
    
    /**
     * Read bogey data from NBT.
     */
    public static CarriageBogey read(CompoundTag tag, TrackGraph graph, DimensionPalette dimensions) {
        // Stub: actual implementation reconstructs bogey from saved data
        // This requires registry lookups which are handled at runtime
        return null;
    }
    
    /**
     * Update positions and angles for rendering.
     */
    public void updateAngles(boolean leading, float angle) {
        angles.set(leading, angle);
    }
    
    public void updatePositions(boolean leading, Vec3 position) {
        positions.set(leading, position);
    }
    
    /**
     * Get the anchor position for this bogey.
     */
    public Vec3 getAnchorPosition() {
        // Stub: returns zero vector, actual implementation averages leading/trailing positions
        return Vec3.ZERO;
    }
    
    /**
     * Get the dimension this bogey is currently in.
     */
    public net.minecraft.resources.ResourceKey<Level> getDimension() {
        // Stub: returns the dimension from the travelling point
        return null;
    }
}
