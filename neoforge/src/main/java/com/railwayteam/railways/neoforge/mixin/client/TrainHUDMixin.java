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

package com.railwayteam.railways.neoforge.mixin.client;

import com.railwayteam.railways.content.switches.TrainHUDSwitchExtension;
import com.simibubi.create.content.trains.TrainHUD;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TrainHUD.class, remap = false)
public class TrainHUDMixin {
    @Inject(method = "renderOverlay", at = @At("HEAD"), require = 0)
    private static void renderOverlayHook(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // In 1.21.1, DeltaTracker provides partial ticks and screen dimensions are in GuiGraphics
        float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        TrainHUDSwitchExtension.renderOverlay(graphics, partialTicks, width, height);
    }
}