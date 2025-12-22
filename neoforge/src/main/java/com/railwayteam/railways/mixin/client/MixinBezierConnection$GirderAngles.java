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

import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.simibubi.create.content.trains.track.BezierConnection;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@OnlyIn(Dist.CLIENT)
@Mixin(targets = "com.simibubi.create.content.trains.track.BezierConnection$GirderAngles", remap = false)
public class MixinBezierConnection$GirderAngles {
	private static final double RAIL_OFFSET_STANDARD = 0.9649999737739563D;
	private static final double RAIL_OFFSET_WIDE_ADD = 0.5D;
	private static final double RAIL_OFFSET_NARROW_SUB = 7D / 16D;

	@ModifyArg(
		method = "<init>",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"),
		index = 0
	)
	private double railways$adjustGirderOffset(double scale, @Local(argsOnly = true) BezierConnection bc) {
		if (Math.abs(scale - RAIL_OFFSET_STANDARD) > 1.0E-6)
			return scale;

		var trackType = bc.getMaterial().trackType;
		if (trackType == CRTrackMaterials.CRTrackType.WIDE_GAUGE)
			return scale + RAIL_OFFSET_WIDE_ADD;
		if (trackType == CRTrackMaterials.CRTrackType.NARROW_GAUGE || trackType == CRTrackMaterials.CRTrackType.UNIVERSAL)
			return scale - RAIL_OFFSET_NARROW_SUB;
		return scale;
	}
}
