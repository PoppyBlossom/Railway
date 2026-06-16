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

package com.railwayteam.railways.neoforge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm.RestorationTarget;
import com.simibubi.create.content.trains.entity.TrainRelocationPacket;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// earlier priority to bypass OPAC protections, which freak out about there being no entity associated with the relocation
@Mixin(value = TrainRelocationPacket.class, priority = 500)
public class TrainRelocationPacketMixin {
    @Shadow @Final UUID trainId;
    @Shadow @Final BlockPos pos;
    @Shadow @Final BezierTrackPointLocation hoveredBezier;
    @Shadow @Final boolean direction;
    @Shadow @Final Vec3 lookAngle;

    @WrapOperation(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;closerThan(Lnet/minecraft/core/Position;D)Z", ordinal = 0))
    private boolean unrestrictRange(Vec3 instance, Position pos, double distance, Operation<Boolean> original,
                                    @Local ServerPlayer sender) {
        if (sender.isCreative() && CRConfigs.server().unlimitedCreativeRelocation.get())
            return true;
        return original.call(instance, pos, distance);
    }

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void relocateShadowTrain(ServerPlayer sender, CallbackInfo ci) {
        RestorationTarget target = new RestorationTarget(sender.level(), pos, hoveredBezier, direction, lookAngle);
        ShadowRealm.handleTrainRelocationPacket(sender, trainId, target, ci);
    }
}
