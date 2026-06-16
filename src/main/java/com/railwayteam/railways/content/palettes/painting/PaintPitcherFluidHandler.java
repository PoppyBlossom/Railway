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

package com.railwayteam.railways.content.palettes.painting;

import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.registry.CRFluids;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaintPitcherFluidHandler implements IFluidHandlerItem {
    private ItemStack container;

    public PaintPitcherFluidHandler(ItemStack container) {
        this.container = container;
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (container.getItem() instanceof PaintPitcherItem item) {
            return makeFluidStack(new PitcherColor(item.getColor()), (int) item.getFluidAmount(container));
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return (int) (PaintPitcherItem.FLUID_PER_LEVEL * PaintPitcherItem.MAX_LEVELS);
    }

    @Nullable
    private PitcherColor getColor() {
        if (container.getItem() instanceof PaintPitcherItem item) {
            return new PitcherColor(item.getColor());
        }
        return null;
    }

    private int getLevels() {
        if (!(container.getItem() instanceof PaintPitcherItem item)) return 0;
        return item.getLevels(container);
    }

    @Nullable
    private static PitcherColor getFluidStackColor(@NotNull FluidStack stack) {
        if (!CRFluids.PAINT.is(stack.getFluid()))
            return null;

        PalettesColor fluidColor = readColor(stack);
        if (fluidColor == null)
            return null;

        return new PitcherColor(fluidColor);
    }

    @Nullable
    private static PalettesColor readColor(FluidStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data != null ? data.copyTag() : null;
        return PaintFluid.getColor(tag).orElse(null);
    }

    @Nullable
    private PitcherColor getColorIfValid(@NotNull FluidStack stack) {
        PitcherColor color = getColor();
        PitcherColor fluidColor = getFluidStackColor(stack);
        if (fluidColor == null) return null;

        if (color != null && !color.equals(fluidColor)) {
            return null;
        }

        return color == null ? fluidColor : color;
    }

    private ItemStack makeFilledStack(PitcherColor color, int levels) {
        return color.getItemEntry().get().copyAsFilledStack(container, levels);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return getColorIfValid(stack) != null;
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        PitcherColor color = getColorIfValid(stack);
        if (color == null) return 0;

        int currentLevels = getLevels();
        int levelCapacity = PaintPitcherItem.MAX_LEVELS - currentLevels;
        if (levelCapacity <= 0) return 0;
        int filledLevels = (int) Math.min(stack.getAmount() / PaintPitcherItem.FLUID_PER_LEVEL, levelCapacity);
        if (filledLevels <= 0) return 0;

        if (action.execute()) {
            container = makeFilledStack(color, currentLevels + filledLevels);
        }

        return (int) (filledLevels * PaintPitcherItem.FLUID_PER_LEVEL);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack stack, FluidAction action) {
        PitcherColor color = getColorIfValid(stack);
        if (color == null) return FluidStack.EMPTY;

        int drained = drainInternal(color, (int) stack.getAmount(), action);
        if (drained <= 0) return FluidStack.EMPTY;

        FluidStack drainedStack = stack.copy();
        drainedStack.setAmount(drained);
        return drainedStack;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        PitcherColor color = getColor();
        if (color == null) return FluidStack.EMPTY;

        int drained = drainInternal(color, maxDrain, action);
        if (drained <= 0) return FluidStack.EMPTY;

        return makeFluidStack(color, drained);
    }

    private int drainInternal(PitcherColor color, int maxDrain, FluidAction action) {
        int currentLevels = getLevels();
        int drainedLevels = (int) Math.min(maxDrain / PaintPitcherItem.FLUID_PER_LEVEL, currentLevels);
        if (drainedLevels <= 0) return 0;

        if (action.execute()) {
            container = makeFilledStack(color, currentLevels - drainedLevels);
        }

        return (int) (drainedLevels * PaintPitcherItem.FLUID_PER_LEVEL);
    }

    private FluidStack makeFluidStack(PitcherColor color, int amount) {
        if (color.isSandyWater())
            return new FluidStack(net.minecraft.world.level.material.Fluids.WATER, amount);

        FluidStack fluidStack = new FluidStack(CRFluids.paintSource(), amount);
        if (color.color() != null) {
            CompoundTag tag = new CompoundTag();
            PaintFluid.setColor(tag, color.color());
            fluidStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        return fluidStack;
    }
}
