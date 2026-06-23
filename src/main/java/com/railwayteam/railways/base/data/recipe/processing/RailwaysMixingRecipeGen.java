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
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.railwayteam.railways.multiloader.fluid.FluidUnits;
import com.railwayteam.railways.registry.CRFluids;
import com.railwayteam.railways.registry.CRPalettes;
import com.railwayteam.railways.util.FluidUtils;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class RailwaysMixingRecipeGen extends MixingRecipeGen {

    Object
        PAINT_RED = paintDye(PalettesColor.RED, DyeColor.RED),
        PAINT_ORANGE = paintDye(PalettesColor.ORANGE, DyeColor.ORANGE),
        PAINT_YELLOW = paintDye(PalettesColor.YELLOW, DyeColor.YELLOW),
        PAINT_LIME = paintDye(PalettesColor.LIME, DyeColor.LIME),
        PAINT_GREEN = paintDye(PalettesColor.GREEN, DyeColor.GREEN),
        PAINT_CYAN = paintDye(PalettesColor.CYAN, DyeColor.CYAN),
        PAINT_LIGHT_BLUE = paintDye(PalettesColor.LIGHT_BLUE, DyeColor.LIGHT_BLUE),
        PAINT_BLUE = paintDye(PalettesColor.BLUE, DyeColor.BLUE),
        PAINT_PURPLE = paintDye(PalettesColor.PURPLE, DyeColor.PURPLE),
        PAINT_MAGENTA = paintDye(PalettesColor.MAGENTA, DyeColor.MAGENTA),
        PAINT_PINK = paintDye(PalettesColor.PINK, DyeColor.PINK),
        PAINT_WHITE = paintDye(PalettesColor.WHITE, DyeColor.WHITE),
        PAINT_LIGHT_GRAY = paintDye(PalettesColor.LIGHT_GRAY, DyeColor.LIGHT_GRAY),
        PAINT_GRAY = paintDye(PalettesColor.GRAY, DyeColor.GRAY),
        PAINT_BLACK = paintDye(PalettesColor.BLACK, DyeColor.BLACK),
        PAINT_BROWN = paintDye(PalettesColor.BROWN, DyeColor.BROWN)
    ;

    Object
        PAINT_GRANITE = paintStone(PalettesColor.GRANITE, AllPaletteStoneTypes.GRANITE),
        PAINT_DRIPSTONE = paintStone(PalettesColor.DRIPSTONE, AllPaletteStoneTypes.DRIPSTONE),
        PAINT_OCHRUM = paintStone(PalettesColor.OCHRUM, AllPaletteStoneTypes.OCHRUM),
        PAINT_DIORITE = paintStone(PalettesColor.DIORITE, AllPaletteStoneTypes.DIORITE),
        PAINT_LIMESTONE = paintStone(PalettesColor.LIMESTONE, AllPaletteStoneTypes.LIMESTONE),
        PAINT_TUFF = paintStone(PalettesColor.TUFF, AllPaletteStoneTypes.TUFF),
        PAINT_SCORCHIA = paintStone(PalettesColor.SCORCHIA, AllPaletteStoneTypes.SCORCHIA)
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

    Object
        PAINT_ORANGE_SECONDARY = paintMix$(PalettesColor.ORANGE, PalettesColor.RED, PalettesColor.YELLOW),
        PAINT_LIME_SECONDARY = paintMix$(PalettesColor.LIME, PalettesColor.GREEN, PalettesColor.WHITE),
        PAINT_CYAN_SECONDARY = paintMix$(PalettesColor.CYAN, PalettesColor.BLUE, PalettesColor.GREEN),
        PAINT_LIGHT_BLUE_SECONDARY = paintMix$(PalettesColor.LIGHT_BLUE, PalettesColor.BLUE, PalettesColor.WHITE),
        PAINT_PURPLE_SECONDARY = paintMix$(PalettesColor.PURPLE, PalettesColor.BLUE, PalettesColor.RED),
        PAINT_MAGENTA_SECONDARY = paintMix$(PalettesColor.MAGENTA, PalettesColor.PURPLE, PalettesColor.PINK),
        PAINT_PINK_SECONDARY = paintMix$(PalettesColor.PINK, PalettesColor.RED, PalettesColor.WHITE),
        PAINT_LIGHT_GRAY_SECONDARY = paintMix$(PalettesColor.LIGHT_GRAY, PalettesColor.GRAY, PalettesColor.WHITE),
        PAINT_GRAY_SECONDARY = paintMix$(PalettesColor.GRAY, PalettesColor.BLACK, PalettesColor.WHITE)
    ;

    public RailwaysMixingRecipeGen(PackOutput output, CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Railways.MOD_ID);

        for (CRPalettes.Styles style : CRPalettes.Styles.values()) {
            for (PalettesColor color : PalettesColor.values()) {
                if (color.isNetherite()) continue;
                PalettesColor finalColor = color;
                CRPalettes.Styles finalStyle = style;
                createWithDeferredId(
                    () -> {
                        ResourceLocation loc = Railways.asResource("palettes/dyeing/" +
                            BuiltInRegistries.BLOCK.getKey(finalStyle.get(finalColor).get()).getPath());
                        if (finalStyle != CRPalettes.Styles.FLYWHEEL) {
                            EmiRecipeDefaultsGen.DEFAULT_RECIPES.add(
                                Railways.asResource(getRecipeType().getId().getPath() + "/" + loc.getPath()));
                        }
                        return loc;
                    },
                    b -> b
                        .require(style.dyeGroupTag)
                        .require(Ingredients.palettesPaint(color, PaintPitcherItem.FLUID_PER_LEVEL))
                        .output(style.get(color))
                );
            }
        }
    }

    private Supplier<ResourceLocation> paintLoc(PalettesColor color) {
        return () -> {
            ResourceLocation loc = Railways.asResource("palettes/dye/" + color.getSerializedName());
            EmiRecipeDefaultsGen.DEFAULT_RECIPES.add(
                Railways.asResource(getRecipeType().getId().getPath() + "/" + loc.getPath()));
            return loc;
        };
    }

    private Object paintDye(PalettesColor color, DyeColor dyeColor) {
        return createWithDeferredId(paintLoc(color), b -> {
            b.require(Ingredients.dye(dyeColor));
            b.require(Ingredients.bindingAgent());
            b.require(Fluids.WATER, (int) FluidUnits.bucket());
            FluidUtils.addFluidOutput(b, CRFluids.paintSource(), FluidUnits.bucket(), PaintFluid.setColor(new CompoundTag(), color));
            return b;
        });
    }

    private Object paintStone(PalettesColor color, AllPaletteStoneTypes stoneType) {
        return createWithDeferredId(paintLoc(color), b -> {
            b.require(stoneType.getBaseBlock().get());
            b.require(Ingredients.bindingAgent());
            b.require(Fluids.WATER, (int) FluidUnits.bucket());
            b.requiresHeat(HeatCondition.HEATED);
            FluidUtils.addFluidOutput(b, CRFluids.paintSource(), FluidUnits.bucket(), PaintFluid.setColor(new CompoundTag(), color));
            return b;
        });
    }

    private Object paintMix(PalettesColor result, PalettesColor colorA, PalettesColor colorB, boolean makeDefault) {
        return createWithDeferredId(
            () -> {
                ResourceLocation loc = Railways.asResource("palettes/dye/" + result.getSerializedName()
                    + "_from_" + colorA.getSerializedName() + "_" + colorB.getSerializedName());
                if (makeDefault) {
                    EmiRecipeDefaultsGen.DEFAULT_RECIPES.add(
                        Railways.asResource(getRecipeType().getId().getPath() + "/" + loc.getPath()));
                }
                return loc;
            },
            b -> {
                b.require(Ingredients.palettesPaint(colorA, PaintPitcherItem.FLUID_PER_LEVEL));
                b.require(Ingredients.palettesPaint(colorB, PaintPitcherItem.FLUID_PER_LEVEL));
                FluidUtils.addFluidOutput(b, CRFluids.paintSource(), PaintPitcherItem.FLUID_PER_LEVEL * 2,
                    PaintFluid.setColor(new CompoundTag(), result));
                return b;
            }
        );
    }

    private Object paintMix(PalettesColor result, PalettesColor colorA, PalettesColor colorB) {
        return paintMix(result, colorA, colorB, true);
    }

    private Object paintMix$(PalettesColor result, PalettesColor colorA, PalettesColor colorB) {
        return paintMix(result, colorA, colorB, false);
    }
}
