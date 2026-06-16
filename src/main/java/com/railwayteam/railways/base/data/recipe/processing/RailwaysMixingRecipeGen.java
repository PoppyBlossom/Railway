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
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.railwayteam.railways.multiloader.fluid.FluidUnits;
import com.railwayteam.railways.registry.CRFluids;
import com.railwayteam.railways.registry.CRItems;
import com.railwayteam.railways.registry.CRTags;
import com.railwayteam.railways.util.FluidUtils;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class RailwaysMixingRecipeGen extends MixingRecipeGen {

    Object
        PAINT_RED = paintDye(PalettesColor.RED, Items.RED_DYE),
        PAINT_ORANGE = paintDye(PalettesColor.ORANGE, Items.ORANGE_DYE),
        PAINT_YELLOW = paintDye(PalettesColor.YELLOW, Items.YELLOW_DYE),
        PAINT_LIME = paintDye(PalettesColor.LIME, Items.LIME_DYE),
        PAINT_GREEN = paintDye(PalettesColor.GREEN, Items.GREEN_DYE),
        PAINT_CYAN = paintDye(PalettesColor.CYAN, Items.CYAN_DYE),
        PAINT_LIGHT_BLUE = paintDye(PalettesColor.LIGHT_BLUE, Items.LIGHT_BLUE_DYE),
        PAINT_BLUE = paintDye(PalettesColor.BLUE, Items.BLUE_DYE),
        PAINT_PURPLE = paintDye(PalettesColor.PURPLE, Items.PURPLE_DYE),
        PAINT_MAGENTA = paintDye(PalettesColor.MAGENTA, Items.MAGENTA_DYE),
        PAINT_PINK = paintDye(PalettesColor.PINK, Items.PINK_DYE),
        PAINT_WHITE = paintDye(PalettesColor.WHITE, Items.WHITE_DYE),
        PAINT_LIGHT_GRAY = paintDye(PalettesColor.LIGHT_GRAY, Items.LIGHT_GRAY_DYE),
        PAINT_GRAY = paintDye(PalettesColor.GRAY, Items.GRAY_DYE),
        PAINT_BLACK = paintDye(PalettesColor.BLACK, Items.BLACK_DYE),
        PAINT_BROWN = paintDye(PalettesColor.BROWN, Items.BROWN_DYE)
    ;

    Object
        PAINT_GRANITE = paintStone(PalettesColor.GRANITE, AllPaletteStoneTypes.GRANITE),
        PAINT_DRIPSTONE = paintStone(PalettesColor.DRIPSTONE, AllPaletteStoneTypes.DRIPSTONE),
        PAINT_DIORITE = paintStone(PalettesColor.DIORITE, AllPaletteStoneTypes.DIORITE),
        PAINT_TUFF = paintStone(PalettesColor.TUFF, AllPaletteStoneTypes.TUFF)
    ;

    Object
        PAINT_MAROON = paintMix(PalettesColor.MAROON, PalettesColor.RED, PalettesColor.BLACK),
        PAINT_VERMILION = paintMix(PalettesColor.VERMILION, PalettesColor.RED, PalettesColor.ORANGE),
        PAINT_CHARTREUSE = paintMix(PalettesColor.CHARTREUSE, PalettesColor.YELLOW, PalettesColor.GREEN),
        PAINT_OLIVE_GREEN = paintMix(PalettesColor.OLIVE_GREEN, PalettesColor.LIME, PalettesColor.GREEN),
        PAINT_PINE_GREEN = paintMix(PalettesColor.PINE_GREEN, PalettesColor.GREEN, PalettesColor.BLACK),
        PAINT_SEA_GREEN = paintMix(PalettesColor.SEA_GREEN, PalettesColor.CYAN, PalettesColor.BLACK),
        PAINT_TURQUOISE = paintMix(PalettesColor.TURQUOISE, PalettesColor.CYAN, PalettesColor.WHITE),
        PAINT_ROYAL_BLUE = paintMix(PalettesColor.ROYAL_BLUE, PalettesColor.BLUE, PalettesColor.BLACK)
    ;

    public RailwaysMixingRecipeGen(PackOutput output, CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Railways.MOD_ID);
    }

    private Supplier<ResourceLocation> paintLoc(PalettesColor color) {
        return () -> Railways.asResource("palettes/dye/" + color.getSerializedName());
    }

    private Object paintDye(PalettesColor color, ItemLike dye) {
        return createWithDeferredId(paintLoc(color), b -> {
            b.require(Ingredient.of(dye));
            b.require(CRTags.AllItemTags.BINDING_AGENTS.tag);
            b.require(Fluids.WATER, (int) FluidUnits.bucket());
            FluidUtils.addFluidOutput(b, CRFluids.paintSource(), FluidUnits.bucket(), PaintFluid.setColor(new CompoundTag(), color));
            return b;
        });
    }

    private Object paintStone(PalettesColor color, AllPaletteStoneTypes stoneType) {
        return createWithDeferredId(paintLoc(color), b -> {
            b.require(stoneType.getBaseBlock().get());
            b.require(CRTags.AllItemTags.BINDING_AGENTS.tag);
            b.require(Fluids.WATER, (int) FluidUnits.bucket());
            b.requiresHeat(HeatCondition.HEATED);
            FluidUtils.addFluidOutput(b, CRFluids.paintSource(), FluidUnits.bucket(), PaintFluid.setColor(new CompoundTag(), color));
            return b;
        });
    }

    private Object paintMix(PalettesColor result, PalettesColor colorA, PalettesColor colorB) {
        return createWithDeferredId(
            () -> Railways.asResource("palettes/dye/" + result.getSerializedName()
                + "_from_" + colorA.getSerializedName() + "_" + colorB.getSerializedName()),
            b -> {
                b.require(Ingredient.of(CRItems.FILLED_PITCHERS.get(colorA.ordinal()).get()));
                b.require(Ingredient.of(CRItems.FILLED_PITCHERS.get(colorB.ordinal()).get()));
                FluidUtils.addFluidOutput(b, CRFluids.paintSource(), PaintPitcherItem.FLUID_PER_LEVEL * 2,
                    PaintFluid.setColor(new CompoundTag(), result));
                return b;
            }
        );
    }
}
