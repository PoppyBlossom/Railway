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

package com.railwayteam.railways.compat.tracks.mods;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.compat.Mods;
import com.railwayteam.railways.compat.tracks.GenericTrackCompat;
import net.minecraft.resources.ResourceLocation;

public class BygTrackCompat extends GenericTrackCompat {
    BygTrackCompat() {
        super(Mods.BYG);
    }

    @Override
    protected ResourceLocation getSlabLocation(String name) {
        // Prefer modern namespace when available, but keep legacy fallback for older ports.
        String namespace = Mods.isModLoaded("biomeswevegone") ? "biomeswevegone" : "byg";
        return ResourceLocation.fromNamespaceAndPath(namespace, name + "_slab");
    }

    private static boolean registered = false;
    public static void register() {
        if (!Mods.BYG.isLoaded && !GenericTrackCompat.isDataGen())
            return;
        if (registered) {
            Railways.LOGGER.error("Duplicate registration of BYG track compat");
            return;
        }
        registered = true;
        Railways.LOGGER.info("Registering tracks for Oh The Biomes You'll Go");
        new BygTrackCompat().register(
            "aspen",
            "baobab",
            "blue_enchanted",
            "bulbis",
            "cika",
            "cypress",
            "ebony",
            "embur",
            "ether",
            "fir",
            "green_enchanted",
            "holly",
            "imparius",
            "jacaranda",
            "lament",
            "mahogany",
            "maple",
            "nightshade",
            "palm",
            "pine",
            "rainbow_eucalyptus",
            "redwood",
            "skyris",
            "sythian",
            "white_mangrove",
            "willow",
            "witch_hazel",
            "zelkova"
        );
    }
}
