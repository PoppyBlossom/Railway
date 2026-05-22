/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.shadow_realm;

import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global manager for the Shadow Realm system.
 * Handles banishing, restoring, and killing trains.
 */
public class ShadowRealm {
    private static final Map<String, Train> BANISHED_TRAINS = new HashMap<>();

    /**
     * Banish a train to the shadow realm
     * @param key Unique key for this banishment
     * @param train The train to banish
     * @param registries Registries for NBT writing
     */
    public static void banish(String key, Train train, HolderLookup.Provider registries) {
           BANISHED_TRAINS.put(key, train);
    }

    /**
     * Restore a train from the shadow realm to the world
     * @param key The key of the banished train
     * @return The train data, or null if not found
     */
    public static Train restore(String key) {
        return BANISHED_TRAINS.remove(key);
    }

    /**
     * Permanently delete a train from the shadow realm
     * @param key The key of the banished train
     */
    public static void kill(String key) {
        BANISHED_TRAINS.remove(key);
    }

    /**
     * Check if a train exists in the shadow realm
     * @param key The key to check
     */
    public static boolean exists(String key) {
        return BANISHED_TRAINS.containsKey(key);
    }

    /**
     * Get all banished trains
     */
    public static Map<String, Train> getAllBanished() {
        return new HashMap<>(BANISHED_TRAINS);
    }

    /**
     * Clear all banished trains (for testing/debugging)
     */
    public static void clearAll() {
        BANISHED_TRAINS.clear();
    }
}
