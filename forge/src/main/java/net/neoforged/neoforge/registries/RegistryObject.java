package net.neoforged.neoforge.registries;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Compile-time stub for NeoForge's DeferredHolder (which replaced RegistryObject in 1.21+).
 * This stub provides the necessary method signatures for compilation.
 * The actual DeferredHolder implementation is provided by NeoForge at runtime.
 * 
 * In NeoForge 1.21+, DeferredRegister.register() returns a DeferredHolder<R, T extends R>
 * which provides access to both the registered value and its registry key.
 */
public class RegistryObject<T> {
    // These fields and constructor are only used if this stub is somehow instantiated,
    // which shouldn't happen since NeoForge provides the real implementation at runtime
    private T value;
    private ResourceKey<T> key;
    
    // No-arg constructor for potential edge cases
    public RegistryObject() {
        this.value = null;
        this.key = null;
    }
    
    // Legacy method from old RegistryObject API
    public T get() {
        return value;
    }
    
    // NeoForge 1.21+ DeferredHolder API methods
    public T value() {
        return value;
    }
    
    public ResourceKey<T> getKey() {
        return key;
    }
    
    public ResourceLocation getId() {
        return key != null ? key.location() : null;
    }
    
    public boolean is(ResourceKey<T> key) {
        return this.key != null && this.key.equals(key);
    }
    
    public boolean is(ResourceLocation location) {
        return key != null && key.location().equals(location);
    }
}
