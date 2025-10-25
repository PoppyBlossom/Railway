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

package com.railwayteam.railways.forge.datagen;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.base.data.recipe.RailwaysSequencedAssemblyRecipeGen;
import com.railwayteam.railways.base.data.recipe.RailwaysStandardRecipeGen;
import com.railwayteam.railways.base.data.recipe.forge.RailwaysMechanicalCraftingRecipeGenImpl;
import com.railwayteam.railways.base.data.RailwaysHatOffsetGenerator;
import net.minecraft.data.DataGenerator;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = Railways.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        // Register Registrate providers and simple PackOutput-only providers via the PackGenerator path
        DataGenerator.PackGenerator pack = event.getGenerator().getVanillaPack(true);
        Railways.gatherData(pack);

        // Register providers that require the lookup provider via DataGenerator directly
        boolean runServer = event.includeServer();
        var generator = event.getGenerator();

        generator.addProvider(runServer, RailwaysSequencedAssemblyRecipeGen.create(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(runServer, RailwaysStandardRecipeGen.create(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(runServer, RailwaysMechanicalCraftingRecipeGenImpl.create(generator.getPackOutput(), event.getLookupProvider()));
        generator.addProvider(runServer, new RailwaysHatOffsetGenerator(generator.getPackOutput(), event.getLookupProvider()));
    }
}
