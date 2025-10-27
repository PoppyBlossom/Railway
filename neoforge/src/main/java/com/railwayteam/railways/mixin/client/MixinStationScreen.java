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

package com.railwayteam.railways.mixin.client;

import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.station.AbstractStationScreen;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.station.StationScreen;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = StationScreen.class, remap = false)
public abstract class MixinStationScreen extends AbstractStationScreen {
    @Shadow private EditBox trainNameBox;
    private Checkbox limitEnableCheckbox;
    private List<ResourceLocation> iconTypes;
    private ScrollInput iconTypeScroll;

    private MixinStationScreen(StationBlockEntity te, GlobalStation station) {
        super(te, station);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/station/StationScreen;tickTrainDisplay()V"), remap = true)
    private void initCheckbox(CallbackInfo ci) {
        int x = guiLeft;
        int y = guiTop;
        // TODO 1.21: Checkbox and train icon editing UI requires porting to new API; temporarily disabled.
        // addRenderableWidget(limitEnableCheckbox);
        // icon type scroll temporarily disabled
        iconTypeScroll = null;
    }

    @Inject(method = "tickTrainDisplay", at = @At("HEAD"))
    private void tickIconScroll(CallbackInfo ci) {
        if (iconTypeScroll == null)
            return;
        Train train = displayedTrain.get();

        if (train == null) {
            if (iconTypeScroll.active) {
                iconTypeScroll.active = false;
                removeWidget(iconTypeScroll);
            }

            Train imminentTrain = getImminent();

            if (imminentTrain != null) {
                iconTypeScroll.active = true;
                iconTypeScroll.setState(iconTypes.indexOf(imminentTrain.icon.getId()));
                addRenderableWidget(iconTypeScroll);
            }
        }
    }
}
