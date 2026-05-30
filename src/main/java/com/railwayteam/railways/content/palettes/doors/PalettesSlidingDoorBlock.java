/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

package com.railwayteam.railways.content.palettes.doors;

import org.jetbrains.annotations.Nullable;

import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.registry.CRBlockEntities;
import com.railwayteam.railways.registry.CRBlockSetTypes;
import com.railwayteam.railways.util.EntityUtils;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.IHaveBigOutline;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PalettesSlidingDoorBlock extends DoorBlock implements IWrenchable, IBE<PalettesSlidingDoorBlockEntity>, IHaveBigOutline {
    public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");
    public static final BooleanProperty WINDOWED = BooleanProperty.create("windowed");
    private final boolean folds;
    public final PalettesColor color;

    public static NonNullFunction<Properties, PalettesSlidingDoorBlock> create(boolean folds, PalettesColor color) {
        return p -> new PalettesSlidingDoorBlock(p, folds, color);
    }

    public PalettesSlidingDoorBlock(Properties properties, boolean folds, PalettesColor color) {
        super(CRBlockSetTypes.LOCOMETAL, properties);
        this.folds = folds;
        this.color = color;
        registerDefaultState(defaultBlockState()
            .setValue(VISIBLE, true)
            .setValue(WINDOWED, false));
    }

    public boolean isFoldingDoor() {
        return folds;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(VISIBLE, WINDOWED));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(OPEN) && (state.getValue(VISIBLE)))
            return super.getShape(state, level, pos, context);

        Direction direction = state.getValue(FACING);
        boolean hinge = state.getValue(HINGE) == DoorHingeSide.RIGHT;
        return SlidingDoorShapes.get(direction, hinge, isFoldingDoor());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER || level.getBlockState(pos.below()).is(this);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                    BlockPos pos, BlockPos neighborPos) {
        BlockState blockState = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        if (blockState.isAir())
            return blockState;
        DoubleBlockHalf doubleblockhalf = blockState.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y
            && doubleblockhalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            if (neighborState.is(this) && neighborState.getValue(HALF) != doubleblockhalf) {
                blockState = blockState.setValue(VISIBLE, neighborState.getValue(VISIBLE))
                    .setValue(WINDOWED, neighborState.getValue(WINDOWED));
            } else {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return blockState;
    }

    @Override
    public void setOpen(@Nullable Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
        if (!state.is(this))
            return;
        if (state.getValue(OPEN) == open)
            return;

        level.setBlock(pos, state.setValue(OPEN, open), 10);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState newState = state.cycle(WINDOWED);
        world.setBlock(pos, newState, UPDATE_ALL);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (EntityUtils.isHolding(player, AllItems.WRENCH::isIn)) {
            return InteractionResult.PASS;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public BlockEntityType<? extends PalettesSlidingDoorBlockEntity> getBlockEntityType() {
        return CRBlockEntities.PALETTES_SLIDING_DOOR.get();
    }

    @Override
    public Class<PalettesSlidingDoorBlockEntity> getBlockEntityClass() {
        return PalettesSlidingDoorBlockEntity.class;
    }
}
