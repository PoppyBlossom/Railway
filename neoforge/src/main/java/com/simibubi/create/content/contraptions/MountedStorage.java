/*
 * Compile-time stub for Create's MountedStorage to allow mixins and Railways code to compile.
 * The real implementation is provided by the Create mod at runtime.
 */
package com.simibubi.create.content.contraptions;

import net.minecraft.nbt.CompoundTag;

@SuppressWarnings("unused")
public class MountedStorage {
    /**
     * Default constructor for mounted storage.
     */
    public MountedStorage() {
    }
    
    /**
     * Write storage data to NBT.
     */
    public CompoundTag write() {
        return new CompoundTag();
    }
    
    /**
     * Read storage data from NBT.
     */
    public static MountedStorage read(CompoundTag tag) {
        return new MountedStorage();
    }
    
    /**
     * Get the mounted items storage manager.
     */
    public MountedStorageManager getMountedItems() {
        return new MountedStorageManager(); // Stub return
    }
}
