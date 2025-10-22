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
    // These fields are never actually used since this stub is replaced by NeoForge's
    // DeferredHolder implementation at runtime. They exist only to make the stub
    // structurally valid for compilation.
    private final T value = null;
    private final ResourceKey<T> key = null;
    
    // All methods throw to prevent accidental runtime usage of this stub.
    
    // Legacy method from old RegistryObject API
    public T get() {
        throw new UnsupportedOperationException("This is a compile-time stub and should not be used at runtime.");
    }
    
    // NeoForge 1.21+ DeferredHolder API methods
    public T value() {
        throw new UnsupportedOperationException("This is a compile-time stub and should not be used at runtime.");
    }
    
    public ResourceKey<T> getKey() {
        throw new UnsupportedOperationException("This is a compile-time stub and should not be used at runtime.");
    }
    
    public ResourceLocation getId() {
        throw new UnsupportedOperationException("This is a compile-time stub and should not be used at runtime.");
    }
    
    public boolean is(ResourceKey<T> key) {
        throw new UnsupportedOperationException("This is a compile-time stub and should not be used at runtime.");
    }
    
    public boolean is(ResourceLocation location) {
        throw new UnsupportedOperationException("This is a compile-time stub and should not be used at runtime.");
    }
}
