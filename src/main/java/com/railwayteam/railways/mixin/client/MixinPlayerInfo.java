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

import com.mojang.authlib.GameProfile;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.annotation.mixin.DevEnvMixin;
import com.railwayteam.railways.util.DevCapeUtils;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInfo.class)
public class MixinPlayerInfo {
    @Shadow @Final private GameProfile profile;

    @Unique private boolean railways$texturesLoaded;
    @Unique private static final ResourceLocation DEV_CAPE = Railways.asResource("textures/misc/dev_cape.png");
    @Unique private static final ResourceLocation DEV_SKIN = Railways.asResource("textures/misc/devenv_skin.png");

    // Replaces skin inside the dev env with the conductor skin
    @DevEnvMixin
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void replaceSkinTexture(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerSkin originalSkin = cir.getReturnValue();
        // Replace with dev skin
        PlayerSkin devSkin = new PlayerSkin(
                DEV_SKIN,
                null,
                originalSkin.capeTexture(),
                originalSkin.elytraTexture(),
                PlayerSkin.Model.WIDE, // Default "steve" model
                originalSkin.secure()
        );
        cir.setReturnValue(devSkin);
    }

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void addDevCape(CallbackInfoReturnable<PlayerSkin> cir) {
        if (!railways$texturesLoaded && DevCapeUtils.INSTANCE.useDevCape(profile.getId())) {
            railways$texturesLoaded = true;
            PlayerSkin originalSkin = cir.getReturnValue();
            // Add dev cape
            PlayerSkin skinWithCape = new PlayerSkin(
                    originalSkin.texture(),
                    originalSkin.textureUrl(),
                    DEV_CAPE,
                    originalSkin.elytraTexture(),
                    originalSkin.model(),
                    originalSkin.secure()
            );
            cir.setReturnValue(skinWithCape);
        }
    }
}
