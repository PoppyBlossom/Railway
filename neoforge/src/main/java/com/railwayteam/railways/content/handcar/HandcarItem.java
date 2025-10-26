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

package com.railwayteam.railways.content.handcar;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.mixin_interfaces.IDeployAnywayBlockItem;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType;
import com.railwayteam.railways.util.packet.CurvedTrackHandcarPlacementPacket;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.entity.TravellingPoint.SteerDirection;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackGraphHelper;
import com.simibubi.create.content.trains.graph.TrackGraphLocation;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import com.simibubi.create.content.trains.track.TrackMaterial.TrackType;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem.OverlapResult;
import com.simibubi.create.foundation.utility.CreateLang;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.createmod.catnip.data.Couple;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class HandcarItem extends BlockItem implements IDeployAnywayBlockItem {
    public HandcarItem(Block block, Properties properties) {
        super(block, properties);
    }

    private HandcarBlock getBogeyBlock() {
        return (HandcarBlock) getBlock();
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        if (state.getBlock() instanceof ITrackBlock track) {
            TrackType trackType = track.getMaterial().trackType;
            if (!(trackType == TrackType.STANDARD || trackType == CRTrackType.UNIVERSAL))
                return InteractionResult.FAIL;
            if (level.isClientSide)
                return InteractionResult.SUCCESS;

            Vec3 lookAngle = player.getLookAngle();
            boolean front = track.getNearestTrackAxis(level, pos, state, lookAngle)
                .getSecond() == Direction.AxisDirection.POSITIVE;

            MutableObject<OverlapResult> result = new MutableObject<>(null);
            MutableObject<TrackGraphLocation> resultLoc = new MutableObject<>(null);
            withGraphLocation(level, pos, front, null, (overlap, location) -> {
                result.setValue(overlap);
                resultLoc.setValue(location);
            });

            if (result.getValue().feedback != null) {
                player.displayClientMessage(CreateLang.translateDirect(result.getValue().feedback)
                    .withStyle(ChatFormatting.RED), true);
                AllSoundEvents.DENY.play(level, null, pos, .5f, 1);
                return InteractionResult.FAIL;
            }

            TrackGraphLocation loc = resultLoc.getValue();
            if (loc == null)
                return InteractionResult.FAIL;

            boolean success = placeHandcar(loc, level, player, pos);
            if (success) {
                stack.shrink(1);
            }
            return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }


        return InteractionResult.PASS;
    }

    @ApiStatus.Internal
    @NotNull
    public boolean placeHandcar(TrackGraphLocation trackGraphLocation, Level level, Player player, BlockPos soundPos) {
        TrackGraph graph = trackGraphLocation.graph;
        TrackNode node1 = graph.locateNode(trackGraphLocation.edge.getFirst());
        TrackNode node2 = graph.locateNode(trackGraphLocation.edge.getSecond());
        TrackEdge edge = graph.getConnectionsFrom(node1).get(node2);
        if (edge == null)
            return false;

        double offset = getBogeyBlock().getWheelPointSpacing() / 2;
        TravellingPoint tp1 = new TravellingPoint(node1, node2, edge, trackGraphLocation.position, false);
        TravellingPoint tp2 = new TravellingPoint(node1, node2, edge, trackGraphLocation.position, false);
        tp1.travel(graph, offset, tp1.steer(SteerDirection.NONE, new Vec3(0, 1, 0)));
        tp2.travel(graph, -offset, tp2.steer(SteerDirection.NONE, new Vec3(0, 1, 0)));/*

        tp1.travel(graph, 10, tp1.steer(SteerDirection.NONE, new Vec3(0, 1, 0)));
        tp2.travel(graph, 10, tp2.steer(SteerDirection.NONE, new Vec3(0, 1, 0)));
        tp1.travel(graph, -10, tp1.steer(SteerDirection.NONE, new Vec3(0, 1, 0)));
        tp2.travel(graph, -10, tp2.steer(SteerDirection.NONE, new Vec3(0, 1, 0)));*/

        if (!(level instanceof ServerLevel serverLevel))
            return false;
        makeTrain(
            player.getUUID(),
            graph,
            tp1,
            tp2,
            serverLevel
        );


        AllSoundEvents.CONTROLLER_CLICK.play(level, null, soundPos, 1, 1);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean useOnCurve(TrackBlockOutline.BezierPointSelection selection, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        TrackBlockEntity be = selection.blockEntity();
        BezierTrackPointLocation loc = selection.loc();
        boolean front = player.getLookAngle()
            .dot(selection.direction()) < 0;

        BezierConnection bc = be.getConnections().get(loc.curveTarget());

        TrackType trackType = bc.getMaterial().trackType;
        if (!(trackType == TrackType.STANDARD || trackType == CRTrackType.UNIVERSAL))
            return false;

        CRPackets.PACKETS.send(new CurvedTrackHandcarPlacementPacket(be.getBlockPos(), loc.curveTarget(),
            loc.segment(), front, player.getInventory().selected));
        return true;
    }

    private @Nullable Train makeTrain(UUID owner, TrackGraph graph, TravellingPoint tp1, TravellingPoint tp2,
                                      ServerLevel level) {
        HandcarBlock handcarBlock = getBogeyBlock();
        
        // Build bogey and carriage following Create 1.21.1 patterns
        // CarriageBogey will be created with the TravellingPoints we computed
        // Actual Create constructor: CarriageBogey(AbstractBogeyBlock<?>, boolean, CompoundTag, TravellingPoint, TravellingPoint)
        CarriageBogey leadingBogey;
        try {
            // Create 1.21.1 constructor: type, upsideDown, data, leading point, trailing point
            leadingBogey = new CarriageBogey(handcarBlock, false, new CompoundTag(), tp1, tp2);
        } catch (Exception e) {
            // Fallback: log error if constructor fails
            Railways.LOGGER.warn("CarriageBogey constructor failed for handcar", e);
            return null;
        }
        
        // Single-bogey Carriage (second bogey is null for handcar, spacing = 0)
        Carriage handcarCarriage = new Carriage(leadingBogey, null, 0);
        
        // Build the Train with one carriage
        List<Carriage> carriages = new ArrayList<>();
        carriages.add(handcarCarriage);
        
        List<Integer> carriageSpacing = new ArrayList<>();
        // Single carriage → no spacing list needed
        
        Train train = new Train(
            UUID.randomUUID(), // train ID
            owner,             // owner UUID
            graph,
            carriages,
            carriageSpacing,
            false,             // not double-ended
            0                  // map color
        );
        
        // Mark as handcar via mixin
        ((IHandcarTrain) train).railways$setHandcar(true);
        
        // Register train in the global railway manager
        Create.RAILWAYS.addTrain(train);
        
        // Sync to clients: Create's internal network handles TrainPacket distribution at runtime
        // We rely on Create.RAILWAYS.addTrain(...) to trigger necessary client syncs
        
        // Collect initially occupied signal blocks
        train.collectInitiallyOccupiedSignalBlocks();
        
        Railways.LOGGER.info("Successfully created handcar train {}", train.id);
        return train;
    }

    public static void withGraphLocation(Level level, BlockPos pos, boolean front,
                                         BezierTrackPointLocation targetBezier,
                                         BiConsumer<OverlapResult, TrackGraphLocation> callback) {

        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof ITrackBlock track)) {
            callback.accept(OverlapResult.NO_TRACK, null);
            return;
        }

        List<Vec3> trackAxes = track.getTrackAxes(level, pos, state);
        if (targetBezier == null && trackAxes.size() > 1) {
            callback.accept(OverlapResult.JUNCTION, null);
            return;
        }

        Direction.AxisDirection targetDirection = front ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        TrackGraphLocation location =
            targetBezier != null ? TrackGraphHelper.getBezierGraphLocationAt(level, pos, targetDirection, targetBezier)
                : TrackGraphHelper.getGraphLocationAt(level, pos, targetDirection, trackAxes.get(0));

        if (location == null) {
            callback.accept(OverlapResult.NO_TRACK, null);
            return;
        }

        Couple<TrackNode> nodes = location.edge.map(location.graph::locateNode);
        TrackEdge edge = location.graph.getConnection(nodes);
        if (edge == null)
            return;

        callback.accept(OverlapResult.VALID, location);
    }
}
