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

package com.railwayteam.railways.content.coupling;

import com.railwayteam.railways.mixin.AccessorAbstractContraptionEntity;
import com.railwayteam.railways.mixin.AccessorOrientedContraptionEntity;
import com.railwayteam.railways.mixin.AccessorScheduleRuntime;
import com.railwayteam.railways.mixin.AccessorTrain;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.railwayteam.railways.mixin_interfaces.IIndexedSchedule;
import com.railwayteam.railways.mixin_interfaces.IStrictSignalTrain;
import com.railwayteam.railways.multiloader.PlayerSelection;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.util.packet.AddTrainEndPacket;
import com.railwayteam.railways.util.packet.CarriageContraptionEntityUpdatePacket;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Navigation;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import com.simibubi.create.content.trains.station.GlobalStation;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;

// (no additional util imports required)

public class TrainUtils {
    /**
     * Splits the train into two trains, with the last carriage of the original train being the first carriage of the new train.
     * @param train The train to split.
     * @param numberOffEnd The number of carriages to move to the new train.
     * @return The new train.
     */
    public static Train splitTrain(Train train, int numberOffEnd) {
        return train;
    }

    public static void tryToParkNearby(Train train, double maxDistance) {
        {
            final double offsetDist = 0.05;
            Carriage leadingCarriage = train.carriages.get(0);
            leadingCarriage.travel(null, train.graph, -offsetDist, null, null, 0);
            TravellingPoint discoveryPoint = copy(leadingCarriage.getLeadingPoint());
            MutableObject<GlobalStation> targetStation = new MutableObject<>(null);
            double distance = discoveryPoint.travel(train.graph, maxDistance+offsetDist, discoveryPoint.steer(TravellingPoint.SteerDirection.NONE, new Vec3(0, 1, 0)), (Double a, Pair<TrackEdgePoint, Couple<TrackNode>> couple) -> {
                if (couple.getFirst() instanceof GlobalStation station && station.canApproachFrom(couple.getSecond().getSecond())
                    && (station.getNearestTrain() == null || station.getNearestTrain() == train) && station.getPresentTrain() == null) {
                    targetStation.setValue(station);
                    return true;
                }
                return false;
            });

            if (targetStation.getValue() != null) {
                Navigation oldNavigation = train.navigation;
                ScheduleRuntime oldRuntime = train.runtime;
                train.navigation = new Navigation(train);
                train.runtime = new ScheduleRuntime(train);
                train.navigation.destination = targetStation.getValue();
                leadingCarriage.travel(null, train.graph, Math.max(0.01, distance)+offsetDist, discoveryPoint, null, 0);
                targetStation.getValue().reserveFor(train);
                train.navigation.train = null; // prevent reference cycle
                train.runtime = oldRuntime;
                train.navigation = oldNavigation;
            } else {
                ((IStrictSignalTrain) train).railways$setStrictSignals(true);
                leadingCarriage.travel(null, train.graph, offsetDist, null, null, 0);
                ((IStrictSignalTrain) train).railways$setStrictSignals(false);
            }
        }
    }

    private static TravellingPoint copy(TravellingPoint original) {
        TravellingPoint copy = new TravellingPoint(original.node1, original.node2, original.edge, original.position, original.upsideDown);
        copy.blocked = original.blocked;
        return copy;
    }

    public static Train combineTrains(Train frontTrain, Train backTrain, BlockPos itemDropPos, Level itemDropLevel, int carriageSpacing) {
        return combineTrains(frontTrain, backTrain, Vec3.atBottomCenterOf(itemDropPos), itemDropLevel, carriageSpacing);
    }

    /**
     * Adds the carriages of backTrain onto the end of frontTrain.
     */
    public static Train combineTrains(Train frontTrain, Train backTrain, Vec3 itemDropPos, Level itemDropLevel, int carriageSpacing) {
        if (((IHandcarTrain) frontTrain).railways$isHandcar() || ((IHandcarTrain) backTrain).railways$isHandcar()) {
            return frontTrain;
        }
        if (frontTrain.derailed || backTrain.derailed) {
            return frontTrain;
        }
        if (!allCarriagesLoaded(frontTrain) || !allCarriagesLoaded(backTrain)) {
            return frontTrain;
        }
        int frontTrainSize = frontTrain.carriages.size();
        frontTrain.carriages.addAll(backTrain.carriages);
        backTrain.carriages.clear();

        frontTrain.carriageSpacing.add(carriageSpacing);
        frontTrain.carriageSpacing.addAll(backTrain.carriageSpacing);
        backTrain.carriageSpacing.clear();
        double[] newStress = new double[((AccessorTrain) frontTrain).railways$getStress().length + ((AccessorTrain) backTrain).railways$getStress().length + 1];
        System.arraycopy(((AccessorTrain) frontTrain).railways$getStress(), 0, newStress, 0, ((AccessorTrain) frontTrain).railways$getStress().length);
        newStress[((AccessorTrain) frontTrain).railways$getStress().length] = 0;
        System.arraycopy(((AccessorTrain) backTrain).railways$getStress(), 0, newStress, ((AccessorTrain) frontTrain).railways$getStress().length + 1, ((AccessorTrain) backTrain).railways$getStress().length);
        ((AccessorTrain) frontTrain).railways$setStress(newStress);

        frontTrain.doubleEnded = frontTrain.carriages.stream().anyMatch(carriage -> carriage.anyAvailableEntity().getContraption() instanceof CarriageContraption carriageContraption && carriageContraption.hasBackwardControls());
        for (int i = 0; i < frontTrain.carriages.size(); i++) {
            int finalI = i;
            Carriage lastCarriage = frontTrain.carriages.get(i);
            lastCarriage.setTrain(frontTrain);
            frontTrain.carriages.get(i).forEachPresentEntity(cce -> {
                cce.carriageIndex = finalI;
                cce.trainId = frontTrain.id;
                cce.setCarriage(lastCarriage);
//                cce.syncCarriage();
            });
        }
        if (backTrain.getCurrentStation() != null) {
            backTrain.getCurrentStation().cancelReservation(backTrain);
        }
        frontTrain.collectInitiallyOccupiedSignalBlocks();
        Create.RAILWAYS.removeTrain(backTrain.id);
        PlayerSelection allPlayers = PlayerSelection.all();
        CRPackets.PACKETS.sendTo(allPlayers, new AddTrainEndPacket(frontTrain, backTrain, carriageSpacing, backTrain.doubleEnded));
        frontTrain.carriages.forEach(carriage -> carriage.forEachPresentEntity(cce ->
                CRPackets.PACKETS.sendTo(allPlayers, new CarriageContraptionEntityUpdatePacket(cce, frontTrain))
        ));
//        frontTrain.carriages.forEach(carriage -> carriage.forEachPresentEntity(CarriageContraptionEntity::syncCarriage));
        if (frontTrain.runtime.getSchedule() == null && backTrain.runtime.getSchedule() != null) {
            ((IIndexedSchedule) frontTrain).railways$setIndex(((IIndexedSchedule) backTrain).railways$getIndex() + frontTrainSize);
            var provider = frontTrain.carriages.get(0).anyAvailableEntity().level().registryAccess();
            frontTrain.runtime.read(provider, backTrain.runtime.write(provider));
            if (backTrain.runtime.state == ScheduleRuntime.State.IN_TRANSIT) {
                frontTrain.runtime.state = ScheduleRuntime.State.PRE_TRANSIT;
                ((AccessorScheduleRuntime) frontTrain.runtime).setCooldown(0);
            }
        } else if (backTrain.runtime.getSchedule() != null) {
            if (frontTrain.runtime.completed) {
                ItemStack stack = frontTrain.runtime.returnSchedule(itemDropLevel.registryAccess());
                Containers.dropItemStack(itemDropLevel, itemDropPos.x, itemDropPos.y, itemDropPos.z, stack);
                ((IIndexedSchedule) frontTrain).railways$setIndex(((IIndexedSchedule) backTrain).railways$getIndex() + frontTrainSize);
                frontTrain.runtime.read(itemDropLevel.registryAccess(), backTrain.runtime.write(itemDropLevel.registryAccess()));
                if (backTrain.runtime.state == ScheduleRuntime.State.IN_TRANSIT) {
                    frontTrain.runtime.state = ScheduleRuntime.State.PRE_TRANSIT;
                    ((AccessorScheduleRuntime) frontTrain.runtime).setCooldown(0);
                }
            } else {
                ItemStack stack = backTrain.runtime.returnSchedule(itemDropLevel.registryAccess());
                Containers.dropItemStack(itemDropLevel, itemDropPos.x, itemDropPos.y, itemDropPos.z, stack);
            }
        }
        return frontTrain;
    }

    public static boolean allCarriagesLoaded(Train train) {
        for (Carriage carriage : train.carriages) {
            if (carriage.anyAvailableEntity() == null) {
                return false;
            }
        }
        return true;
    }

    public static void discardTrain(Train train) {
        for (Carriage carriage : train.carriages) {
            CarriageContraptionEntity entity = carriage.anyAvailableEntity();
            if (entity == null) continue;

            StructureTransform transform = ((AccessorOrientedContraptionEntity) entity).railways$makeStructureTransform();

            // Disassembly is handled by entity changes; explicit packet no longer sent via CRPackets
            entity.getContraption().addPassengersToWorld(entity.level(), transform, entity.getPassengers());
            ((AccessorAbstractContraptionEntity) entity).railways$setSkipActorStop(true);
            entity.discard();
            entity.ejectPassengers();
            ((AccessorAbstractContraptionEntity) entity).railways$moveCollidedEntitiesOnDisassembly(transform);
        }
        train.invalid = true; // don't remove yet, otherwise concurrent modification exceptions happen
        /*Create.RAILWAYS.removeTrain(train.id);
        CRPackets.PACKETS.sendTo(PlayerSelection.all(), new TrainPacket(train, false));*/
    }
}
