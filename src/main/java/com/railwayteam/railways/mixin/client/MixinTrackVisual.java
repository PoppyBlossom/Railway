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

package com.railwayteam.railways.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.custom_tracks.casing.CasingRenderUtils;
import com.railwayteam.railways.mixin_interfaces.IGetBezierConnection;
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackMaterial.TrackType;
import com.simibubi.create.content.trains.track.TrackShape;
import com.simibubi.create.content.trains.track.TrackVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visual.AbstractVisual;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static com.railwayteam.railways.content.custom_tracks.casing.CasingRenderUtils.casingPositions;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.NARROW_GAUGE;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.WIDE_GAUGE;

/**
 * Mixin to add track casing rendering to TrackVisual.
 * In Create 1.21.1, TrackVisual extends AbstractVisual (not AbstractBlockEntityVisual) and
 * implements BlockEntityVisual<TrackBlockEntity>, ShaderLightVisual manually.
 */
@Mixin(value = TrackVisual.class, remap = false)
public abstract class MixinTrackVisual extends AbstractVisual implements IGetBezierConnection {
    // Shadow fields from TrackVisual
    @Shadow
    protected TrackBlockEntity blockEntity;
    
    @Shadow
    protected BlockPos pos;
    
    @Shadow
    protected BlockPos visualPos;

    // Pseudo-constructor for mixin - required for extending AbstractVisual
    protected MixinTrackVisual(VisualizationContext ctx, Level level, float partialTick) {
        super(ctx, level, partialTick);
    }

    @Shadow
    public abstract void _delete();

	@Unique
    @Nullable
    private BezierConnection bezierConnection = null;

    @Unique
    private final List<Pair<TransformedInstance, BlockPos>> casingData = new ArrayList<>();

    /**
     * Helper method to update light on a FlatLit instance.
     * TrackVisual in Create 1.21.1 does not have an updateLight method (it uses ShaderLightVisual instead),
     * so we provide our own implementation.
     */
    @Unique
    private static void railways$updateLightOnInstance(FlatLit instance, Level level, BlockPos pos) {
        int packedLight = LevelRenderer.getLightColor(level, pos);
        instance.light(packedLight).handle().setChanged();
    }

    @Override
    public @Nullable BezierConnection getBezierConnection() {
        return bezierConnection;
    }
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onCtor(VisualizationContext context, TrackBlockEntity track, float partialTick, CallbackInfo ci) {
        railways$makeCasingData(true);
    }

    @Inject(method = "createInstance", at = @At("HEAD"))
    private void preCreateInstance(BezierConnection bc, CallbackInfoReturnable<?> cir) {
        this.bezierConnection = bc;
    }

    @Inject(method = "update", at = @At(value = "RETURN", ordinal = 0))
    private void updateWithoutConnections(CallbackInfo ci) { //otherwise it visually stays when an encased track is broken
        this._delete();
        railways$makeCasingData(false);
    }

    @Inject(method = "update", at = @At(value = "RETURN", ordinal = 1))
    private void updateWithConnections(CallbackInfo ci) {
        railways$makeCasingData(true);
    }

    // TrackVisual in Create 1.21.1 doesn't have an updateLight method (uses ShaderLightVisual instead),
    // so we update lights when the visual is created/updated in railways$makeCasingData

    @Inject(method = "_delete", at = @At("HEAD"))
    private void railways$_delete(CallbackInfo ci) {
        casingData.forEach((data) -> data.getFirst().delete());
        casingData.clear();
    }

    @Unique
    private void railways$makeCasingData(boolean connections) {
        PoseStack ms = new PoseStack();
        TransformStack.of(ms)
            .translate(this.visualPos)
            .nudge((int) this.pos.asLong());

        // Safe check: ensure the mixin was applied before casting
        if (!(this.blockEntity instanceof IHasTrackCasing casing))
            return;

        BlockState blockState = this.blockEntity.getBlockState();
        SlabBlock casingBlock = casing.getTrackCasing();
        if (casingBlock != null) {
            TrackShape shape = blockState.getValue(TrackBlock.SHAPE);
            if (CRBlockPartials.TRACK_CASINGS.containsKey(shape)) {
                ms.pushPose();
                if (this.blockEntity.isTilted()) {
                    double angle = this.blockEntity.tilt.smoothingAngle.get();
                    switch (blockState.getValue(TrackBlock.SHAPE)) {
                        case ZO -> TransformStack.of(ms)
                            .rotateXDegrees((float) -angle);
                        case XO -> TransformStack.of(ms)
                            .rotateZDegrees((float) angle);
                        default -> {
                            // No tilt transform needed for other shapes (e.g., custom CR_NDZ)
                        }
                    }
                }
                TrackType trackType = null;
                if (blockState.getBlock() instanceof TrackBlock trackBlock)
                    trackType = trackBlock.getMaterial().trackType;

                CRBlockPartials.TrackCasingSpec spec = CRBlockPartials.TRACK_CASINGS.get(shape);
                if (casing.isAlternate())
                    spec = spec.getNonNullAltSpec(trackType);
                else
                    spec = spec.getFor(trackType);
                PartialModel rawCasingModel = spec.model;
                CRBlockPartials.ModelTransform transform = spec.transform;

                TransformedInstance casingInstance = CasingRenderUtils.makeCasingInstance(rawCasingModel, casingBlock, instancerProvider());
                casingInstance.setTransform(ms)
                    .rotateX(transform.rx())
                    .rotateY(transform.ry())
                    .rotateZ(transform.rz())
                    .translate(transform.x(), transform.y(), transform.z());
                railways$updateLightOnInstance(casingInstance, this.level, this.pos);
                casingData.add(Pair.of(casingInstance, this.pos));

                for (CRBlockPartials.ModelTransform additionalTransform : spec.additionalTransforms) {
                    TransformedInstance additionalInstance = CasingRenderUtils.makeCasingInstance(rawCasingModel, casingBlock, instancerProvider());
                    additionalInstance.setTransform(ms)
                        .rotateX(additionalTransform.rx())
                        .rotateY(additionalTransform.ry())
                        .rotateZ(additionalTransform.rz())
                        .translate(additionalTransform.x(), additionalTransform.y(), additionalTransform.z());
                    railways$updateLightOnInstance(additionalInstance, this.level, this.pos);
                    casingData.add(Pair.of(additionalInstance, this.pos.offset(Mth.floor(additionalTransform.x()), Mth.floor(additionalTransform.y()), Mth.floor(additionalTransform.z()))));
                }
                ms.popPose();
            }
        }

        if (connections) {
            for (BezierConnection bc : this.blockEntity.getConnections().values()) {
                if (!bc.isPrimary()) continue;
                casingBlock = ((IHasTrackCasing) bc).getTrackCasing();
                if (casingBlock != null) {
                    int heightDiff = Math.abs(bc.bePositions.get(false).getY() - bc.bePositions.get(true).getY());
                    double shiftDown = ((IHasTrackCasing) bc).isAlternate() && heightDiff > 0 ? -0.25 : 0;
                    if (heightDiff / bc.getLength() <= 4 / 30d) {
                        for (Vec3 pos : casingPositions(bc)) {
                            TransformedInstance casingInstance = CasingRenderUtils.makeCasingInstance(heightDiff==0 ? CRBlockPartials.TRACK_CASING_FLAT :
                                CRBlockPartials.TRACK_CASING_FLAT_THICK, casingBlock, instancerProvider());
                            casingInstance.setTransform(ms)
                                .translate(0, shiftDown, 0)
                                .translate(pos.x, pos.y, pos.z)
                                .scale(1.001f);
                            BlockPos relativePos = BlockPos.containing(this.pos.getX() + pos.x, this.pos.getY() + pos.y, this.pos.getZ() + pos.z);
                            railways$updateLightOnInstance(casingInstance, this.level, relativePos);
                            casingData.add(Pair.of(casingInstance, relativePos));
                        }
                    } else {
                        // In 1.21, baked segment data is stored as arrays inside a single SegmentAngles
                        BezierConnection.SegmentAngles segment = bc.getBakedSegments();
                        int len = segment.tieTransform.length;

                        for (int i = 1; i < len; i++) {
                            if (i % 2 == 0) continue;

                            PoseStack.Pose tiePose = segment.tieTransform[i];
                            BlockPos lightPos = segment.lightPosition[i];

                            TransformedInstance casingInstance = CasingRenderUtils.makeCasingInstance(heightDiff == 0 ? CRBlockPartials.TRACK_CASING_FLAT :
                                CRBlockPartials.TRACK_CASING_FLAT_THICK, casingBlock, instancerProvider());
                            casingInstance.setTransform(ms)
                                .mul(tiePose)
                                .translate(0, (i % 4) * 0.001f, 0)
                                .translate(0, shiftDown, 0)
                                .scale(1.001f);
                            BlockPos relativePos = new BlockPos(this.pos.getX() + lightPos.getX(), this.pos.getY() + lightPos.getY(), this.pos.getZ() + lightPos.getZ());
                            railways$updateLightOnInstance(casingInstance, this.level, relativePos);
                            casingData.add(Pair.of(casingInstance, relativePos));

                            TrackType trackType = bc.getMaterial().trackType;
                            if (trackType == WIDE_GAUGE) {
                                for (boolean first : Iterate.trueAndFalse) {
                                    for (boolean inner : Iterate.trueAndFalse) {
                                        PoseStack.Pose transform = segment.railTransforms[i].get(first);

                                        TransformedInstance casingInstance2 = CasingRenderUtils.makeCasingInstance(heightDiff == 0 ? CRBlockPartials.TRACK_CASING_FLAT :
                                            CRBlockPartials.TRACK_CASING_FLAT_THICK, casingBlock, instancerProvider());
                                        casingInstance2.setTransform(ms)
                                            .mul(transform)
                                            .translate(0, (i % 4) * 0.001f, 0)
                                            .translate((first ? -(61 / 64.) : -(1 / 32.)) + (inner ? 0 : (first ? 1 : -1)), shiftDown, 0);
                                        BlockPos relativePos2 = new BlockPos(this.pos.getX() + lightPos.getX(), this.pos.getY() + lightPos.getY(), this.pos.getZ() + lightPos.getZ());
                                        railways$updateLightOnInstance(casingInstance2, this.level, relativePos2);
                                        casingData.add(Pair.of(casingInstance2, relativePos2));
                                    }
                                }
                            } else {
                                for (boolean first : Iterate.trueAndFalse) {
                                    PoseStack.Pose transform = segment.railTransforms[i].get(first);

                                    TransformedInstance casingInstance2 = CasingRenderUtils.makeCasingInstance(heightDiff == 0 ? CRBlockPartials.TRACK_CASING_FLAT :
                                        CRBlockPartials.TRACK_CASING_FLAT_THICK, casingBlock, instancerProvider());
                                    casingInstance2.setTransform(ms)
                                        .mul(transform)
                                        .translate(0, (i % 4) * 0.001f, 0)
                                        .translate(-0.5 + (trackType == NARROW_GAUGE ? (first ? 0.5 : -0.5) : 0), shiftDown, 0);
                                    BlockPos relativePos2 = new BlockPos(this.pos.getX() + lightPos.getX(), this.pos.getY() + lightPos.getY(), this.pos.getZ() + lightPos.getZ());
                                    railways$updateLightOnInstance(casingInstance2, this.level, relativePos2);
                                    casingData.add(Pair.of(casingInstance2, relativePos2));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
