/*
 * Compatibility shim for ForgeCapabilities removed in NeoForge 1.21+
 * 
 * In NeoForge 1.21+, use net.neoforged.neoforge.capabilities.Capabilities instead.
 * 
 * TODO: Replace all ForgeCapabilities usage with the new Capabilities class
 * and remove this shim once the migration is complete.
 */
package net.neoforged.neoforge.common.capabilities;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Compatibility shim for ForgeCapabilities.
 * Provides basic Capability instances for common capability types.
 */
public class ForgeCapabilities {
    /**
     * Fluid handler capability - for blocks/entities that can store or transfer fluids.
     */
    public static final Capability<IFluidHandler> FLUID_HANDLER = 
        new Capability<>("forge:fluid_handler", IFluidHandler.class);

    /**
     * Item handler capability - for blocks/entities that can store or transfer items.
     */
    public static final Capability<IItemHandler> ITEM_HANDLER =
        new Capability<>("forge:item_handler", IItemHandler.class);

    /**
     * Energy capability - for blocks/entities that can store or transfer energy.
     */
    public static final Capability<Object> ENERGY =
        new Capability<>("forge:energy", Object.class);

    private ForgeCapabilities() {
        // Utility class
    }
}
