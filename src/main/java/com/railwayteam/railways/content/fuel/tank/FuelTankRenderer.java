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

package com.railwayteam.railways.content.fuel.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FuelTankRenderer extends SafeBlockEntityRenderer<FuelTankBlockEntity> {

    public FuelTankRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(FuelTankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        if (!be.isController())
            return;
        if (!be.window) {
            return;
        }

        LerpedFloat fluidLevel = be.getFluidLevel();
        if (fluidLevel == null)
            return;

        float capHeight = 1 / 4f;
        float tankHullWidth = 1 / 16f + 1 / 128f;
        float minPuddleHeight = 1 / 16f;
        float totalHeight = be.height - 2 * capHeight - minPuddleHeight;

        float level = fluidLevel.getValue(partialTicks);
        if (level < 1 / (512f * totalHeight))
            return;
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);

        FluidTank tank = be.tankInventory;
        FluidStack fluidStack = tank.getFluid();

        if (fluidStack.isEmpty())
            return;

        float xMin = tankHullWidth;
        float xMax = be.width - tankHullWidth;
        float yMin = totalHeight - clampedLevel + capHeight + minPuddleHeight / 2;
        float yMax = totalHeight + capHeight + minPuddleHeight / 2;
        float zMin = tankHullWidth;
        float zMax = be.width - tankHullWidth;

        ms.pushPose();
        ms.translate(0, clampedLevel - totalHeight, 0);

        CatnipServices.FLUID_RENDERER.renderFluidBox(fluidStack.getFluid().defaultFluidState(), xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light, false, true);
        ms.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(FuelTankBlockEntity be) {
        return be.isController();
    }

}
