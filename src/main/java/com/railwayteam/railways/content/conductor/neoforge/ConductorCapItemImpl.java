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

package com.railwayteam.railways.content.conductor.neoforge;

import com.railwayteam.railways.content.conductor.ConductorCapItem;
import com.railwayteam.railways.content.conductor.ConductorCapModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ConductorCapItemImpl extends ConductorCapItem {
	protected ConductorCapItemImpl(Properties props, DyeColor color) {
		super(props, color);
	}

	private static int diffuseTintRgb(DyeColor color) {
		// Approximated vanilla diffuse colors formerly provided by DyeColor.getTextureDiffuseColors()
		float r;
		float g;
		float b;
		switch (color) {
			case WHITE -> { r = 1.0f; g = 1.0f; b = 1.0f; }
			case ORANGE -> { r = 0.85f; g = 0.5f; b = 0.2f; }
			case MAGENTA -> { r = 0.7f; g = 0.3f; b = 0.85f; }
			case LIGHT_BLUE -> { r = 0.4f; g = 0.6f; b = 0.85f; }
			case YELLOW -> { r = 0.9f; g = 0.9f; b = 0.2f; }
			case LIME -> { r = 0.5f; g = 0.8f; b = 0.1f; }
			case PINK -> { r = 0.95f; g = 0.5f; b = 0.65f; }
			case GRAY -> { r = 0.3f; g = 0.3f; b = 0.3f; }
			case LIGHT_GRAY -> { r = 0.6f; g = 0.6f; b = 0.6f; }
			case CYAN -> { r = 0.3f; g = 0.5f; b = 0.6f; }
			case PURPLE -> { r = 0.5f; g = 0.25f; b = 0.7f; }
			case BLUE -> { r = 0.2f; g = 0.3f; b = 0.7f; }
			case BROWN -> { r = 0.4f; g = 0.3f; b = 0.2f; }
			case GREEN -> { r = 0.4f; g = 0.5f; b = 0.2f; }
			case RED -> { r = 0.6f; g = 0.2f; b = 0.2f; }
			case BLACK -> { r = 0.1f; g = 0.1f; b = 0.1f; }
			default -> { r = 1.0f; g = 1.0f; b = 1.0f; }
		}

		int ri = Math.min(255, Math.max(0, Math.round(r * 255f)));
		int gi = Math.min(255, Math.max(0, Math.round(g * 255f)));
		int bi = Math.min(255, Math.max(0, Math.round(b * 255f)));
		return (ri << 16) | (gi << 8) | bi;
	}

	public static ConductorCapItem create(Properties props, DyeColor color) {
		return new ConductorCapItemImpl(props, color);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Nonnull
			@Override
			public Model getGenericArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				return ConductorCapModel.of(itemStack, _default, entityLiving);
			}

			@Override
			public int getArmorLayerTintColor(ItemStack stack, LivingEntity entity, net.minecraft.world.item.ArmorMaterial.Layer layer, int layerIdx, int fallbackColor) {
				if (stack.getItem() instanceof ConductorCapItem cap && layer.dyeable()) {
					int rgb = diffuseTintRgb(cap.color);
					int r = (rgb >> 16) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = rgb & 0xFF;

					// Tiny darken so stripes aren't overly bright.
					final float darken = 0.95f;
					r = Math.min(255, Math.max(0, Math.round(r * darken)));
					g = Math.min(255, Math.max(0, Math.round(g * darken)));
					b = Math.min(255, Math.max(0, Math.round(b * darken)));

					return 0xFF000000 | (r << 16) | (g << 8) | b;
				}
				return fallbackColor;
			}
		});
		super.initializeClient(consumer);
	}

	@Nullable
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return textureStr;
	}

	@Override
	public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
		return true;
	}
}
