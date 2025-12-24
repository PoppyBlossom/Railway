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

package com.railwayteam.railways.multiloader.neoforge;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.common.extensions.IItemExtension;

public class PlatformAbstractionHelperImpl {
    /**
     * Return the burn time for the given item.
     * <p>
     * Preferred path: use NeoForge's IItemExtension#getBurnTime which takes
     * an ItemStack and optional RecipeType (for NBT-sensitive burn times). If
     * that returns a negative value (requesting vanilla fallback) we use the
     * vanilla/NeoForge fuel map {@link AbstractFurnaceBlockEntity#getFuel()}.
     */
    public static int getBurnTime(Item item) {
        ItemStack stack = item.getDefaultInstance();
        int burn = ((IItemExtension) item).getBurnTime(stack, null);
        if (burn >= 0) {
            return burn;
        }

        // Fallback to the vanilla fuel map. Mark deprecation suppression since
        // NeoForge provides a data map replacement but the vanilla API still exists.
        @SuppressWarnings("deprecation")
        int fallback = AbstractFurnaceBlockEntity.getFuel().getOrDefault(item, 0);
        return fallback;
    }
}
