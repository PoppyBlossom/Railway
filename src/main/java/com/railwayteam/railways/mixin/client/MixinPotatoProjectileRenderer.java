/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
 */

package com.railwayteam.railways.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileRenderer;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.railwayteam.railways.registry.CRBlockPartials;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PotatoProjectileRenderer.class)
public class MixinPotatoProjectileRenderer {
    @WrapOperation(
        method = "render(Lcom/simibubi/create/content/equipment/potatoCannon/PotatoProjectileEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderStatic(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;I)V"
        )
    )
    private void renderPaintBlob(
        ItemRenderer instance,
        ItemStack stack,
        ItemDisplayContext displayContext,
        int combinedLight,
        int combinedOverlay,
        com.mojang.blaze3d.vertex.PoseStack poseStack,
        MultiBufferSource buffer,
        Level level,
        int seed,
        Operation<Void> original
    ) {
        if (stack.getItem() instanceof PaintPitcherItem item) {
            PalettesColor color = item.getColor();
            PartialModel model = color == null ? CRBlockPartials.PAINT_STRIPPER_BLOB : CRBlockPartials.PAINT_BLOBS.get(color);
            PartialItemModelRenderer.of(stack, displayContext, poseStack, buffer, combinedOverlay)
                .render(model.get(), combinedLight);
        } else {
            original.call(instance, stack, displayContext, combinedLight, combinedOverlay, poseStack, buffer, level, seed);
        }
    }
}
