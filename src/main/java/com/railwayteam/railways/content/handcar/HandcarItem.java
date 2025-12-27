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
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.AddTrainPacket;
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
import net.minecraft.network.chat.Component;
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

import net.createmod.catnip.platform.CatnipServices;

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
            var nearestAxis = track.getNearestTrackAxis(level, pos, state, lookAngle);
            Vec3 axisVec = nearestAxis.getFirst();
            boolean front = nearestAxis.getSecond() == Direction.AxisDirection.POSITIVE;

            Direction.Axis axis = Math.abs(axisVec.x) > Math.abs(axisVec.z) ? Direction.Axis.X : Direction.Axis.Z;
            Direction assemblyDirection = axis == Direction.Axis.X
                ? (front ? Direction.EAST : Direction.WEST)
                : (front ? Direction.SOUTH : Direction.NORTH);

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

            boolean success = placeHandcar(loc, level, player, pos, assemblyDirection);
            if (success) {
                stack.shrink(1);
            }
            return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }


        return InteractionResult.PASS;
    }

    @ApiStatus.Internal
    @NotNull
    public boolean placeHandcar(TrackGraphLocation trackGraphLocation, Level level, Player player, BlockPos soundPos, Direction assemblyDirection) {
        TrackGraph graph = trackGraphLocation.graph;
        TrackNode node1 = graph.locateNode(trackGraphLocation.edge.getFirst());
        TrackNode node2 = graph.locateNode(trackGraphLocation.edge.getSecond());
        
        if (node1 == null || node2 == null)
            return false;
        
        TrackEdge edge = graph.getConnectionsFrom(node1).get(node2);
        if (edge == null)
            return false;

        double offset = getBogeyBlock().getWheelPointSpacing() / 2;
        TravellingPoint tp1 = new TravellingPoint(node1, node2, edge, trackGraphLocation.position, false);
        TravellingPoint tp2 = new TravellingPoint(node1, node2, edge, trackGraphLocation.position, false);
        tp1.travel(graph, offset, tp1.steer(SteerDirection.NONE, new Vec3(0, 1, 0)));
        tp2.travel(graph, -offset, tp2.steer(SteerDirection.NONE, new Vec3(0, 1, 0)));

        if (!(level instanceof ServerLevel serverLevel))
            return false;
        Train train = makeTrain(
            player.getUUID(),
            graph,
            tp1,
            tp2,
            serverLevel,
            soundPos,
            assemblyDirection
        );

        if (train == null)
            return false;

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
                                      ServerLevel level, BlockPos referencePos, Direction assemblyDirection) {
        HandcarBlock handcarBlock = getBogeyBlock();
        CarriageContraption contraption = new CarriageContraption(assemblyDirection);

        BlockPos tempOrigin = new BlockPos(referencePos.getX(), level.getMaxBuildHeight() - 5, referencePos.getZ());
        BlockPos handcarWorldPos = tempOrigin;
        
        Direction safeAssemblyDirection = assemblyDirection.getAxis().isVertical() ? Direction.EAST : assemblyDirection;
        BlockPos seatWorldPos = tempOrigin.relative(safeAssemblyDirection);

        BlockState handcarState = handcarBlock.defaultBlockState().setValue(AbstractBogeyBlock.AXIS, safeAssemblyDirection.getAxis());
        BlockState seatState = AllBlocks.SEATS.get(net.minecraft.world.item.DyeColor.RED).get().defaultBlockState();

        level.setBlock(handcarWorldPos, handcarState, 18);
        level.setBlock(seatWorldPos, seatState, 18);

        boolean assembled = false;
        try {
            assembled = contraption.assemble(level, handcarWorldPos);
        } catch (Throwable t) {
            Railways.LOGGER.error("Failed to assemble handcar contraption", t);
        } finally {
            level.setBlock(handcarWorldPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 18);
            level.setBlock(seatWorldPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 18);
        }

        if (!assembled)
            return null;

        CompoundTag handcarNbt = new CompoundTag();
        CompoundTag bogeyData = new CompoundTag();
        bogeyData.putBoolean("UpsideDown", false);
        bogeyData.putString("BogeyStyle", "railways:handcar");
        handcarNbt.put("BogeyData", bogeyData);
        handcarNbt.putString("id", "railways:bogey");

        BlockPos localHandcarPos = BlockPos.ZERO;
        BlockPos localSeatPos = seatWorldPos.subtract(handcarWorldPos);

        var info = contraption.getBlocks().get(localHandcarPos);
        if (info != null) {
            contraption.getBlocks().put(localHandcarPos,
                new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo(localHandcarPos, info.state(), handcarNbt));
        }

        if (!contraption.getSeats().contains(localSeatPos))
            contraption.getSeats().add(localSeatPos);

        CarriageBogey bogey = new CarriageBogey(handcarBlock, false, new CompoundTag(), tp1, tp2);
        Carriage carriage = new Carriage(bogey, null, 0);
        Train train = new Train(UUID.randomUUID(), owner, graph, List.of(carriage), new ArrayList<>(), true, 0);

        ((IHandcarTrain) train).railways$setHandcar(true);

        carriage.setContraption(level, contraption);

        train.name = Component.translatable("block.railways.handcar");
        train.collectInitiallyOccupiedSignalBlocks();
        Create.RAILWAYS.addTrain(train);
        CatnipServices.NETWORK.sendToAllClients(new AddTrainPacket(train));

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
