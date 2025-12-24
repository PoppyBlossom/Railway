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

import com.railwayteam.railways.registry.CRTrackMaterials;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.content.trains.track.TrackPlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@OnlyIn(Dist.CLIENT)
@Mixin(value = TrackPlacement.class, remap = false)
public class MixinTrackPlacementClient {
	private static final double RAIL_OFFSET_STANDARD = 15D / 16D;
	private static final double RAIL_OFFSET_WIDE_ADD = 0.5D;
	private static final double RAIL_OFFSET_NARROW_SUB = 7D / 16D;

	@ModifyArg(
		method = "clientTick",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"),
		index = 0
	)
	private static double railways$adjustPlacementOverlayRailOffset(double scale) {
		if (Math.abs(scale - RAIL_OFFSET_STANDARD) > 1.0E-6)
			return scale;

		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null)
			return scale;

		TrackMaterial material = TrackMaterial.fromItem(player.getMainHandItem().getItem());
		if (material == null)
			return scale;

		var trackType = material.trackType;
		if (trackType == CRTrackMaterials.CRTrackType.WIDE_GAUGE)
			return scale + RAIL_OFFSET_WIDE_ADD;
		if (trackType == CRTrackMaterials.CRTrackType.NARROW_GAUGE || trackType == CRTrackMaterials.CRTrackType.UNIVERSAL)
			return scale - RAIL_OFFSET_NARROW_SUB;
		return scale;
	}
}
