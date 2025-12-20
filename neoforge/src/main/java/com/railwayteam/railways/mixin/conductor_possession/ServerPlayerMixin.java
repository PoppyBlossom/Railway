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

package com.railwayteam.railways.mixin.conductor_possession;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.content.conductor.ConductorPossessionController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes sure the server does not move the player viewing a camera to the camera's position
 *
 * Confirmed compatible with SecurityCraft
 *
 * Updated for 1.21.1: In 1.21.1, the tick() method calls entity.isAlive() on the camera without
 * a null check. We need to ensure the camera is never null, so instead of canceling setCamera
 * entirely, we let it proceed but intercept absMoveTo to prevent position sync.
 */
@Mixin(value = ServerPlayer.class, priority = 1200)
public abstract class ServerPlayerMixin {
	@Shadow public abstract Entity getCamera();

	/**
	 * Redirect the isAlive() call on the camera entity to return false if the camera is null
	 * or if we're possessing a conductor. This prevents the absMoveTo from being called at all.
	 * 
	 * In 1.21.1, vanilla code structure is:
	 *   Entity entity = this.getCamera();
	 *   if (entity != this && entity.isAlive()) {
	 *       this.absMoveTo(...);
	 *   }
	 */
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isAlive()Z"), require = 0)
	private boolean railways$redirectIsAlive(Entity entity) {
		if (entity == null) {
			return false;
		}
		// If possessing a conductor or viewing a SecurityCraft camera, return false to skip the absMoveTo
		if (ConductorPossessionController.isPossessingConductor((ServerPlayer)(Object)this)) {
			return false;
		}
		if (entity.getClass().getName().equals("net.geforcemods.securitycraft.entity.camera.SecurityCamera")) {
			return false;
		}
		return entity.isAlive();
	}

	/**
	 * Still prevent setting camera to ConductorEntity through normal means,
	 * but we handle the direct field access elsewhere.
	 */
	@Inject(method = "setCamera", at = @At("HEAD"), cancellable = true)
	private void railways$railways$setCamera(Entity entityToSpectate, CallbackInfo ci) {
		if (entityToSpectate instanceof ConductorEntity) ci.cancel();
	}
}
