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

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Used for when you register blocks on a per-loader
 * basis, usually due to doing fluids
 */
public class AbstractionUtils {    public static BlockEntry<?> getFluidTankBlockEntry() {
    return com.railwayteam.railways.util.neoforge.AbstractionUtilsImpl.getFluidTankBlockEntry();
    }    public static boolean isInstanceOfFuelTankBlockEntity(BlockEntity blockEntity) {
    return com.railwayteam.railways.util.neoforge.AbstractionUtilsImpl.isInstanceOfFuelTankBlockEntity(blockEntity);
    }    public static boolean isInstanceOfFuelTankMountedStorageType(MountedFluidStorageType<?> type) {
    return com.railwayteam.railways.util.neoforge.AbstractionUtilsImpl.isInstanceOfFuelTankMountedStorageType(type);
    }    public static BlockEntry<?> getPortableFuelInterfaceBlockEntry() {
    return com.railwayteam.railways.util.neoforge.AbstractionUtilsImpl.getPortableFuelInterfaceBlockEntry();
    }    public static boolean portableFuelInterfaceBlockHasState(BlockState state) {
    return com.railwayteam.railways.util.neoforge.AbstractionUtilsImpl.portableFuelInterfaceBlockHasState(state);
    }
}
