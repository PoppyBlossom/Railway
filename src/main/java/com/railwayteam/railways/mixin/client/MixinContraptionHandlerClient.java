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

import com.railwayteam.railways.config.CRConfigs;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ContraptionHandlerClient.class, priority = 600)
public class MixinContraptionHandlerClient {

    @Inject(method = "handleSpecialInteractions", at = @At("HEAD"), cancellable = true)
    private static void shadowRealmShortcut(
        AbstractContraptionEntity contraptionEntity,
        Player player,
        BlockPos localPos,
        Direction side,
        InteractionHand interactionHand,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!player.isShiftKeyDown()) return;
        if (!(contraptionEntity instanceof CarriageContraptionEntity entity)) return;

        ItemStack stack = player.getItemInHand(interactionHand);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean hasShadowHammer = tag != null && tag.getBoolean("ShadowHammer");
        boolean universalWrench = player.isCreative() && CRConfigs.client().universalShadowWrench.get();

        if (!hasShadowHammer && !universalWrench) return;

        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ChatScreen("/snr shadow_realm banish " + entity.trainId + " "));
        cir.setReturnValue(true);
    }
}
