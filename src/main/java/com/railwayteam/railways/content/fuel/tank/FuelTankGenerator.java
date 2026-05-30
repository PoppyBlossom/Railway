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

package com.railwayteam.railways.content.fuel.tank;

import com.railwayteam.railways.Railways;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;

/**
 * Blockstate datagen for the fuel tank. The vertical position (single/top/middle/bottom)
 * comes from the TOP/BOTTOM flags and the window cutout from SHAPE; each state maps to the
 * matching hand-authored {@code block/fuel_tank/block_*} model. Previously this was left to
 * the Fabric subproject and disabled on NeoForge, so Registrate emitted a default cube_all
 * stub that shadowed the real blockstate and broke the tank's appearance / CT merging (#287).
 */
public class FuelTankGenerator {

    public <T extends Block> void generate(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov) {
        prov.getVariantBuilder(ctx.getEntry())
                .forAllStates(state -> {
                    boolean top = state.getValue(FuelTankBlock.TOP);
                    boolean bottom = state.getValue(FuelTankBlock.BOTTOM);
                    FuelTankBlock.Shape shape = state.getValue(FuelTankBlock.SHAPE);

                    String position = top && bottom ? "single" : top ? "top" : bottom ? "bottom" : "middle";
                    String suffix = shape == FuelTankBlock.Shape.PLAIN ? "" : "_" + shape.getSerializedName();

                    ModelFile model = prov.models()
                            .getExistingFile(Railways.asResource("block/fuel_tank/block_" + position + suffix));
                    return ConfiguredModel.builder()
                            .modelFile(model)
                            .build();
                });
    }
}
