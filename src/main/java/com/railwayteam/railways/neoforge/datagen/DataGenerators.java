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

package com.railwayteam.railways.neoforge.datagen;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.base.data.recipe.RailwaysSequencedAssemblyRecipeGen;
import com.railwayteam.railways.base.data.recipe.RailwaysStandardRecipeGen;
import com.railwayteam.railways.base.data.recipe.neoforge.RailwaysMechanicalCraftingRecipeGenImpl;
import com.railwayteam.railways.base.data.RailwaysHatOffsetGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = Railways.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        // Register Registrate providers via the PackGenerator path
        DataGenerator.PackGenerator pack = event.getGenerator().getVanillaPack(true);
        Railways.gatherData(pack);

        // Register providers that require the lookup provider via DataGenerator directly
        boolean runServer = event.includeServer();
        var generator = event.getGenerator();
        var lookupProvider = event.getLookupProvider();
        var packOutput = generator.getPackOutput();

        // Register each recipe provider separately with unique names to avoid duplication
        // Consolidate all recipes into a single provider since they all have the same name
        RailwaysSequencedAssemblyRecipeGen sequencedAssembly = RailwaysSequencedAssemblyRecipeGen.create(packOutput, lookupProvider);
        RailwaysStandardRecipeGen standardRecipes = RailwaysStandardRecipeGen.create(packOutput, lookupProvider);
        RailwaysMechanicalCraftingRecipeGenImpl mechanicalCrafting = RailwaysMechanicalCraftingRecipeGenImpl.createImpl(packOutput, lookupProvider);
        
        // Create a single wrapper provider that combines all recipes
        generator.addProvider(runServer, new RecipeProvider(packOutput, lookupProvider) {
            @Override
            protected void buildRecipes(@NotNull RecipeOutput output) {
                // Call buildRecipes on each provider to populate their internal recipe lists
                sequencedAssembly.buildRecipes(output);
                standardRecipes.buildRecipes(output);
                mechanicalCrafting.buildRecipes(output);
            }
        });
        
        generator.addProvider(runServer, new RailwaysHatOffsetGenerator(packOutput, lookupProvider));
    }
}
