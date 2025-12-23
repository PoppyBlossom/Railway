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

package com.railwayteam.railways.content.conductor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ConductorCapLayer<T extends ConductorEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	public ConductorCapLayer(RenderLayerParent<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight,
	                   @NotNull T conductor, float limbSwing, float limbSwingAmount, float partialTicks,
	                   float ageInTicks, float netHeadYaw, float headPitch) {
		ItemStack headItem = conductor.getItemBySlot(EquipmentSlot.HEAD);
		if (!(headItem.getItem() instanceof ConductorCapItem capItem)) {
			return;
		}

		ConductorCapModel<?> model = ConductorCapModel.of(headItem, (net.minecraft.client.model.HumanoidModel<?>) this.getParentModel(), conductor);
		ResourceLocation texture = capItem.textureId;
		VertexConsumer consumer = buffer.getBuffer(model.renderType(texture));
		model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
	}
}
