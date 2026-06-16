/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.util;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class FluidUtils {
    public static boolean canUseAsFuelStorage(BlockEntity be) {
        return com.railwayteam.railways.util.neoforge.FluidUtilsImpl.canUseAsFuelStorage(be);
    }

    /**
     * @param o Either a FluidStack (forge & fabric) or FluidVariant (fabric)
     * @return The fluid
     * @throws IllegalArgumentException If any object that isn't an instance of FluidStack or FluidVariant is passed.
     */
    public static Fluid getFluid(Object o) {
        return com.railwayteam.railways.util.neoforge.FluidUtilsImpl.getFluid(o);
    }

    @SuppressWarnings("rawtypes")
    public static void addFluidOutput(ProcessingRecipeBuilder b, Fluid fluid, long amount, @Nullable CompoundTag nbt) {
        FluidStack stack = new FluidStack(fluid, (int) amount);
        if (nbt != null) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
        }
        b.output(stack);
    }
}
