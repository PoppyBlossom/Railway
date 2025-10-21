/*
 * Compatibility shim for ICapabilityProvider removed in NeoForge 1.21+
 */
package net.neoforged.neoforge.common.capabilities;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

/**
 * Compatibility shim for ICapabilityProvider.
 */
public interface ICapabilityProvider {
    <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side);
}
