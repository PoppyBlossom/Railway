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

package com.railwayteam.railways.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Position;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(TrainRelocator.class)
public class MixinTrainRelocator {
    @Shadow
    static UUID relocatingTrain;

    @Inject(method = "getRelocating", at = @At("HEAD"), cancellable = true)
    private static void getShadowRelocating(LevelAccessor level, CallbackInfoReturnable<Train> cir) {
        // If relocatingTrain matches a shadow train, return it for display
        if (ShadowRealm.clientShadowRestoringTrain != null
            && relocatingTrain != null
            && relocatingTrain.equals(ShadowRealm.clientShadowRestoringTrainId)) {
            cir.setReturnValue(ShadowRealm.clientShadowRestoringTrain);
        }
    }

    @Inject(method = "clientTick", at = @At("HEAD"), remap = false)
    private static void clearShadowRestoringTrain(CallbackInfo ci) {
        // Clear shadow state when no longer relocating a shadow train
        if (ShadowRealm.clientShadowRestoringTrainId != null
            && !ShadowRealm.clientShadowRestoringTrainId.equals(relocatingTrain)) {
            ShadowRealm.clientShadowRestoringTrain = null;
            ShadowRealm.clientShadowRestoringTrainId = null;
        }
    }

    @WrapOperation(method = {"clientTick", "onClicked"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;closerThan(Lnet/minecraft/core/Position;D)Z"))
    private static boolean unrestrictRange(Vec3 instance, Position pos, double distance, Operation<Boolean> original,
                                           @Local(name = "player") LocalPlayer player) {
        // Bypass range check for shadow train relocation or creative relocation
        if (ShadowRealm.clientShadowRestoringTrainId != null
            && ShadowRealm.clientShadowRestoringTrainId.equals(relocatingTrain)) {
            return true;
        }
        if (player.isCreative() && CRConfigs.server().unlimitedCreativeRelocation.get())
            return true;

        return original.call(instance, pos, distance);
    }
}
