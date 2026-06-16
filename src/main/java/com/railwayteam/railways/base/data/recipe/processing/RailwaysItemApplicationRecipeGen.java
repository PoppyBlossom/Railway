/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

package com.railwayteam.railways.base.data.recipe.processing;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.base.data.compat.emi.EmiRecipeDefaultsGen;
import com.railwayteam.railways.base.data.recipe.RailwaysRecipeProvider.Ingredients;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.registry.CRPalettes.Styles;
import com.simibubi.create.api.data.recipe.ItemApplicationRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public class RailwaysItemApplicationRecipeGen extends ItemApplicationRecipeGen {

    @SuppressWarnings("unused")
    public RailwaysItemApplicationRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Railways.MOD_ID);

        for (PalettesColor color : PalettesColor.values()) {
            createWithDeferredId(
                () -> Railways.asResource("palettes/flywheels/" + color.getSerializedName()),
                b -> {
                    b.require(Ingredients.flywheel())
                      .require(Styles.RIVETED.get(color))
                      .output(Styles.FLYWHEEL.get(color));
                    EmiRecipeDefaultsGen.DEFAULT_RECIPES.add(
                        Railways.asResource(getRecipeType().getId().getPath() + "/palettes/flywheels/" + color.getSerializedName())
                    );
                    return b;
                }
            );
        }
    }
}
