/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.forge;

import com.mojang.brigadier.CommandDispatcher;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.RailwaysClient;
import com.railwayteam.railways.registry.CRParticleTypes;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

@EventBusSubscriber(Dist.CLIENT)
public class RailwaysClientImpl {
	public static void init() {
		RailwaysClient.init();
		RailwaysImpl.bus.addListener(RailwaysClientImpl::onModelLayerRegistration);
		RailwaysImpl.bus.addListener(RailwaysClientImpl::onBuiltinPackRegistration);
		RailwaysImpl.bus.addListener((RegisterParticleProvidersEvent event) -> CRParticleTypes.registerFactories());
	}

	// region -- Client Commands ---

	private static final Set<Consumer<CommandDispatcher<SharedSuggestionProvider>>> clientCommandConsumers = new HashSet<>();

	public static void registerClientCommands(Consumer<CommandDispatcher<SharedSuggestionProvider>> consumer) {
		clientCommandConsumers.add(consumer);
	}

	@SuppressWarnings({"unchecked", "rawtypes"}) // jank!
	@SubscribeEvent
	public static void onClientCommandRegistration(RegisterClientCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		CommandDispatcher<SharedSuggestionProvider> casted = (CommandDispatcher) dispatcher;
		clientCommandConsumers.forEach(consumer -> consumer.accept(casted));
	}

	// endregion

	// region --- Model Layers ---

	private static final Map<ModelLayerLocation, Supplier<LayerDefinition>> modelLayers = new HashMap<>();

	public static void registerModelLayer(ModelLayerLocation layer, Supplier<LayerDefinition> definition) {
		modelLayers.put(layer, definition);
	}

	public static void onModelLayerRegistration(EntityRenderersEvent.RegisterLayerDefinitions event) {
		modelLayers.forEach(event::registerLayerDefinition);
		modelLayers.clear();
	}

	// endregion

	// region --- Built-in Packs ---

	private record PackInfo(String id, String name) {}

	private static final List<PackInfo> packs = new ArrayList<>();

	public static void registerBuiltinPack(String id, String name) {
		packs.add(new PackInfo(id, name));
	}

	// Based on Create's impl, updated for NeoForge 1.21
	public static void onBuiltinPackRegistration(AddPackFindersEvent event) {
		if (event.getPackType() != PackType.CLIENT_RESOURCES)
			return;

		packs.forEach(pack -> {
			try {
				var modFile = ModList.get().getModFileById(Railways.MOD_ID);
				if (modFile == null) {
					Railways.LOGGER.error("Could not find mod file for " + Railways.MOD_ID);
					return;
				}

				var resourcePath = modFile.getFile().findResource("resourcepacks/" + pack.id);
				
				event.addRepositorySource((consumer) -> {
					PackLocationInfo packInfo = new PackLocationInfo(
						Railways.asResource(pack.id).toString(),
						Component.literal(pack.name),
						PackSource.BUILT_IN,
						java.util.Optional.empty()
					);
					
					PackSelectionConfig selectionConfig = new PackSelectionConfig(
						false,  // required
						Pack.Position.TOP,
						false   // fixedPosition
					);
					
					Pack newPack = Pack.readMetaAndCreate(
						packInfo,
						new Pack.ResourcesSupplier() {
							@Override
							public PathPackResources openPrimary(PackLocationInfo info) {
								return new PathPackResources(info, resourcePath);
							}

							@Override
							public PathPackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
								return new PathPackResources(info, resourcePath);
							}
						},
						PackType.CLIENT_RESOURCES,
						selectionConfig
					);
					
					if (newPack != null) {
						consumer.accept(newPack);
					}
				});
			} catch (Exception e) {
				Railways.LOGGER.error("Failed to register built-in pack: " + pack.id, e);
			}
		});
		
		packs.clear();
	}

	// endregion
}
