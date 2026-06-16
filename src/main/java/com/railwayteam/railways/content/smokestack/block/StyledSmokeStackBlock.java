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

package com.railwayteam.railways.content.smokestack.block;

import com.railwayteam.railways.content.buffer.BlockStateBlockItemGroup;
import com.railwayteam.railways.content.smokestack.RotationType;
import com.railwayteam.railways.content.smokestack.SmokeEmissionParams;
import com.railwayteam.railways.content.smokestack.SmokestackStyle;
import com.railwayteam.railways.util.ShapeWrapper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class StyledSmokeStackBlock extends SmokeStackBlock {

    private final Supplier<BlockStateBlockItemGroup<SmokestackStyle.Context, SmokestackStyle>> cycleGroup;

    public StyledSmokeStackBlock(Properties properties, RotationType rotationType, SmokeEmissionParams emissionParams, ShapeWrapper shape, boolean createsStationarySmoke, Supplier<BlockStateBlockItemGroup<SmokestackStyle.Context, SmokestackStyle>> cycleGroup) {
        super(properties, rotationType, emissionParams, shape, createsStationarySmoke);
        this.cycleGroup = cycleGroup;
    }

    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return cycleGroup.get().get(state.getValue(STYLE)).asStack();
    }
}
