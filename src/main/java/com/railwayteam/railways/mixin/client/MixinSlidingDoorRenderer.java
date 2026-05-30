/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
 */

package com.railwayteam.railways.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.palettes.doors.PalettesSlidingDoorBlock;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(SlidingDoorRenderer.class)
public class MixinSlidingDoorRenderer {
    @WrapOperation(
        method = "renderSafe(Lcom/simibubi/create/content/decoration/slidingDoor/SlidingDoorBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object getPalettesPartials(@SuppressWarnings("rawtypes") Map instance, Object key, Operation<Object> original, @Local BlockState state) {
        if (state.getBlock() instanceof PalettesSlidingDoorBlock block && block.isFoldingDoor()) {
            return CRBlockPartials.FOLDING_DOORS.get(block.color).get(state.getValue(PalettesSlidingDoorBlock.WINDOWED));
        } else {
            return original.call(instance, key);
        }
    }
}
