/*
 * Compatibility shim for Capability API removed in NeoForge 1.21+
 * 
 * This is a minimal implementation to enable compilation during the porting process.
 * The new NeoForge capability system uses a different registration and query mechanism.
 * 
 * TODO: Refactor all capability code to use the new NeoForge 1.21+ capability API
 * and remove this shim once the migration is complete.
 */
package net.neoforged.neoforge.common.capabilities;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility shim for Capability.
 * This is NOT a full reimplementation and should be replaced with proper capability API usage.
 */
public class Capability<T> {
    private final String name;
    private final Class<T> typeClass;

    public Capability(String name, Class<T> typeClass) {
        this.name = name;
        this.typeClass = typeClass;
    }

    public String getName() {
        return name;
    }

    public Class<T> getTypeClass() {
        return typeClass;
    }

    /**
     * Basic capability provider interface for compatibility.
     */
    public interface ICapabilityProvider {
        <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side);
    }
}
