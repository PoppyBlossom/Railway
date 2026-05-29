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

package com.railwayteam.railways.content.custom_tracks.phantom;

import com.railwayteam.railways.content.custom_tracks.NoCollisionCustomTrackBlock;
import com.railwayteam.railways.content.custom_tracks.TransparentSegmentTrackBlock;
import com.railwayteam.railways.registry.CRShapes;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.Affine;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PhantomNarrowGaugeTrackBlock extends TrackBlock implements TransparentSegmentTrackBlock {
    public PhantomNarrowGaugeTrackBlock(Properties properties, TrackMaterial material) {
        super(properties, material);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return getFullShape(state);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter pLevel, BlockPos pPos) {
        return getFullShape(state);
    }

    private VoxelShape getFullShape(BlockState state) {
        switch (state.getValue(SHAPE)) {
            case AE -> {
                return CRShapes.NARROW_TRACK_ASC.get(Direction.EAST);
            }
            case AW -> {
                return CRShapes.NARROW_TRACK_ASC.get(Direction.WEST);
            }
            case AN -> {
                return CRShapes.NARROW_TRACK_ASC.get(Direction.NORTH);
            }
            case AS -> {
                return CRShapes.NARROW_TRACK_ASC.get(Direction.SOUTH);
            }
            case CR_D -> {
                return CRShapes.NARROW_TRACK_CROSS_DIAG;
            }
            case CR_NDX -> {
                return CRShapes.NARROW_TRACK_CROSS_ORTHO_DIAG.get(Direction.SOUTH);
            }
            case CR_NDZ -> {
                return CRShapes.NARROW_TRACK_CROSS_DIAG_ORTHO.get(Direction.SOUTH);
            }
            case CR_O -> {
                return CRShapes.NARROW_TRACK_CROSS;
            }
            case CR_PDX -> {
                return CRShapes.NARROW_TRACK_CROSS_DIAG_ORTHO.get(Direction.EAST);
            }
            case CR_PDZ -> {
                return CRShapes.NARROW_TRACK_CROSS_ORTHO_DIAG.get(Direction.EAST);
            }
            case ND -> {
                return CRShapes.NARROW_TRACK_DIAG.get(Direction.SOUTH);
            }
            case PD -> {
                return CRShapes.NARROW_TRACK_DIAG.get(Direction.EAST);
            }
            case XO -> {
                return CRShapes.NARROW_TRACK_ORTHO.get(Direction.EAST);
            }
            case ZO -> {
                return CRShapes.NARROW_TRACK_ORTHO.get(Direction.SOUTH);
            }
            case TE -> {
                return CRShapes.NARROW_TRACK_ORTHO_LONG.get(Direction.EAST);
            }
            case TW -> {
                return CRShapes.NARROW_TRACK_ORTHO_LONG.get(Direction.WEST);
            }
            case TS -> {
                return CRShapes.NARROW_TRACK_ORTHO_LONG.get(Direction.SOUTH);
            }
            case TN -> {
                return CRShapes.NARROW_TRACK_ORTHO_LONG.get(Direction.NORTH);
            }
            default -> {
            }
        }
        return AllShapes.TRACK_FALLBACK;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (CRTrackMaterials.getBaseFromNarrow(getMaterial()).getBlock() instanceof NoCollisionCustomTrackBlock noCollisionBlock) {
            return noCollisionBlock.getCollisionShape(pState, pLevel, pPos, pContext);
        }
        return super.getCollisionShape(pState, pLevel, pPos, pContext);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public <Self extends Affine<Self>> PartialModel prepareTrackOverlay(Affine<Self> affine, BlockGetter world, BlockPos pos, BlockState state, BezierTrackPointLocation bezierPoint, AxisDirection direction, RenderedTrackOverlayType type) {
        if (bezierPoint == null && !PhantomSpriteManager.isVisible()) {
            affine.scale(0); // hide the cached overlay instance rather than leaving it at the default orientation (#242)
            return null;
        }
        return super.prepareTrackOverlay(affine, world, pos, state, bezierPoint, direction, type);
    }
}
