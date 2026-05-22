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

package com.railwayteam.railways.util;

import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import org.jetbrains.annotations.Contract;

public class ItemUtils {
	@Contract // shut
	public static boolean blocksEndermanView(ItemStack stack, Player wearer, EnderMan enderman) {
		return com.railwayteam.railways.util.neoforge.ItemUtilsImpl.blocksEndermanView(stack, wearer, enderman);
	}

	public static void copyStackData(ItemStack source, ItemStack target) {
		// In 1.21.1, copy components from source to target
		target.applyComponents(source.getComponentsPatch());
	}

	public static net.minecraft.world.InteractionHand oppositeHand(net.minecraft.world.InteractionHand hand) {
		return hand == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.InteractionHand.OFF_HAND : net.minecraft.world.InteractionHand.MAIN_HAND;
	}

	public static boolean isUnbreakable(ItemStack stack) {
		// Check if the Unbreakable component is set
		return stack.has(DataComponents.UNBREAKABLE);
	}
}
