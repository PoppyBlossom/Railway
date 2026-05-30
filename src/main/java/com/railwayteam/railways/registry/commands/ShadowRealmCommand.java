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

package com.railwayteam.railways.registry.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ShadowRealmCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("shadow_realm")
            .requires(cs -> cs.hasPermission(2))
            .then(Commands.literal("banish")
                .then(Commands.argument("train", StringArgumentType.string())
                    .then(Commands.argument("key", StringArgumentType.string())
                        .executes(ctx -> banish(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "train"),
                            StringArgumentType.getString(ctx, "key")
                        ))
                    )
                )
            )
            .then(Commands.literal("restore")
                .then(Commands.argument("key", StringArgumentType.string())
                    .executes(ctx -> restore(
                        ctx.getSource(),
                        StringArgumentType.getString(ctx, "key")
                    ))
                )
            )
            .then(Commands.literal("kill")
                .then(Commands.argument("key", StringArgumentType.string())
                    .executes(ctx -> kill(
                        ctx.getSource(),
                        StringArgumentType.getString(ctx, "key")
                    ))
                )
            );
    }

    private static int banish(CommandSourceStack source, String trainName, String key) {
        // Find train by name
        Train train = Create.RAILWAYS.trains.values().stream()
            .filter(t -> t.name.getString().equals(trainName))
            .findFirst()
            .orElse(null);

        if (train == null) {
            source.sendFailure(Component.literal("Train '" + trainName + "' not found"));
            return 0;
        }

        if (ShadowRealm.exists(key)) {
            source.sendFailure(Component.literal("Key '" + key + "' already in use. Please use a different key."));
            return 0;
        }

        try {
            // Remove from world
            Create.RAILWAYS.removeTrain(train.id);
            
            // Banish to shadow realm
            ShadowRealm.banish(key, train, source.getServer().registryAccess());
            
            source.sendSuccess(() -> Component.literal("Train '")
                .append(train.name)
                .append("' banished to shadow realm with key '")
                .append(key)
                .append("'"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to banish train: " + e.getMessage()));
            return 0;
        }
    }

    private static int restore(CommandSourceStack source, String key) {
        Train restoredTrain = ShadowRealm.restore(key);

        if (restoredTrain == null) {
            source.sendFailure(Component.literal("No train found with key '" + key + "'"));
            return 0;
        }

        try {
            Create.RAILWAYS.addTrain(restoredTrain);
            source.sendSuccess(() -> Component.literal("Restored train '")
                .append(restoredTrain.name)
                .append("' from shadow realm"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to restore train: " + e.getMessage()));
            return 0;
        }
    }

    private static int kill(CommandSourceStack source, String key) {
        if (!ShadowRealm.exists(key)) {
            source.sendFailure(Component.literal("No train found with key '" + key + "'"));
            return 0;
        }

        ShadowRealm.kill(key);
        source.sendSuccess(() -> Component.literal("Train with key '")
            .append(key)
            .append("' permanently deleted from shadow realm"), true);
        return 1;
    }
}
