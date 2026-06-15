/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.PalettesColor;

import net.minecraft.resources.ResourceLocation;

/**
 * Paint fluid registration for the NeoForge port.
 * The full paint fluid type with color-dependent textures will be registered
 * when the NeoForge fluid infrastructure is set up.
 */
public class CRFluids {
    /**
     * Gets the resource location for a paint fluid texture.
     */
    public static ResourceLocation getPaintStillTexture(PalettesColor color) {
        if (color == null) color = PalettesColor.NETHERITE;
        return Railways.asResource("block/palettes/paint/still/" + color.getSerializedName());
    }

    public static ResourceLocation getPaintFlowingTexture(PalettesColor color) {
        if (color == null) color = PalettesColor.NETHERITE;
        return Railways.asResource("block/palettes/paint/flowing/" + color.getSerializedName());
    }

    public static void register() {
        // Paint fluid registration will be handled through Create's registrate system
        // when the full fluid infrastructure is ready
    }
}
