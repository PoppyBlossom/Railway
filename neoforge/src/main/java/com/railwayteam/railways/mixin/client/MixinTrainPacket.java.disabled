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

package com.railwayteam.railways.mixin.client;

import com.railwayteam.railways.compat.journeymap.DummyRailwayMarkerHandler;
import com.simibubi.create.content.trains.entity.TrainPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * EXCLUDED FROM COMPILATION (see neoforge/build.gradle.kts).
 * 
 * This client-side mixin injects into Create's TrainPacket to notify JourneyMap when trains are removed.
 * It's currently excluded because TrainPacket's structure in Create 1.21.1 is still stabilizing.
 * 
 * When re-enabling:
 * 1. Verify Create's TrainPacket.handle() still uses a lambda for train removal (lambda$handle$0)
 * 2. Note: Lambda method names are compiler-generated and may change with Create updates
 * 3. Alternative: Consider targeting handle() directly or using @ModifyVariable
 * 4. Test with JourneyMap integration to ensure train markers are removed correctly
 */
@Mixin(value = TrainPacket.class, remap = false)
public class MixinTrainPacket {
    @Shadow
    UUID trainId;

    // This targets a lambda method in TrainPacket.handle() that removes trains from a map
    // Lambda method names are compiler-generated and fragile - they can change between Create versions
    // If this mixin fails with "target not found", the lambda name likely changed
    // The injection point catches train removal to update JourneyMap markers
    // Alternative: Consider using @ModifyVariable or targeting the handle() method directly
    @Inject(method = "lambda$handle$0", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;"))
    private void catchRemoval(CallbackInfo ci) {
        if (DummyRailwayMarkerHandler.getInstance() != null) {
            DummyRailwayMarkerHandler.getInstance().removeTrain(trainId);
        }
    }
}
