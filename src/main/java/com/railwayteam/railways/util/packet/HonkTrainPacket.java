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

package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.util.UUID;

public class HonkTrainPacket implements C2SPacket {
    final UUID trainId;
    final boolean isHonk;

    public HonkTrainPacket(UUID trainId, boolean isHonk) {
        this.trainId = trainId;
        this.isHonk = isHonk;
    }

    public HonkTrainPacket(FriendlyByteBuf buf) {
        trainId = buf.readUUID();
        isHonk = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(trainId);
        buffer.writeBoolean(isHonk);
    }

    @Override
    public void handle(ServerPlayer sender) {
        // Safe-guard: check Create's registry for the train
        Train train = Create.RAILWAYS.trains.get(trainId);
        if (train == null) return;

        // Try common method names via reflection to trigger honk, otherwise fallback to determineHonk
        try {
            // Candidate methods that might exist in various Create versions
            String[] candidates = new String[]{"honk", "playHonk", "setHonk", "setHornState"};
            boolean invoked = false;
            for (String name : candidates) {
                try {
                    Method m = Train.class.getMethod(name, boolean.class);
                    m.invoke(train, isHonk);
                    invoked = true;
                    break;
                } catch (NoSuchMethodException ignored) {
                }
            }

            if (!invoked) {
                // If no direct method, call determineHonk on server when 'isHonk' is true to let Create decide
                try {
                    // Try Level parameter first, then ServerLevel. Use serverLevel() from the sender for invocation.
                    Method determine = null;
                    try {
                        determine = Train.class.getMethod("determineHonk", net.minecraft.world.level.Level.class);
                    } catch (NoSuchMethodException e1) {
                        try {
                            determine = Train.class.getMethod("determineHonk", net.minecraft.server.level.ServerLevel.class);
                        } catch (NoSuchMethodException e2) {
                            determine = null;
                        }
                    }

                    if (determine != null) {
                        if (isHonk) {
                            determine.invoke(train, sender.serverLevel());
                        } else {
                            // Try to stop honk: look for a stop method
                            try {
                                Method stop = Train.class.getMethod("stopHonk");
                                stop.invoke(train);
                            } catch (NoSuchMethodException ex) {
                                // Nothing to do; many Create versions don't expose an explicit stop method
                            }
                        }
                    } else {
                        // As a last resort, attempt to call determineHonk without arguments if available
                        try {
                            Method determineNoArgs = Train.class.getMethod("determineHonk");
                            if (isHonk) determineNoArgs.invoke(train);
                        } catch (NoSuchMethodException ignored) {
                            Railways.LOGGER.warn("HonkTrainPacket: unable to find a suitable method to trigger train honk for train {}", trainId);
                        }
                    }
                } catch (ReflectiveOperationException e) {
                    Railways.LOGGER.warn("HonkTrainPacket: reflection error when triggering train honk for train {}: {}", trainId, e.toString());
                }
            }
        } catch (ReflectiveOperationException e) {
            Railways.LOGGER.warn("HonkTrainPacket: reflection error when triggering train honk for train {}: {}", trainId, e.toString());
        }
    }
}
