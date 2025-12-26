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

package com.railwayteam.railways.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.RailwaysClient;
import com.railwayteam.railways.content.conductor.ConductorRenderer;
import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceBlockEntity;
import com.railwayteam.railways.content.semaphore.SemaphoreRenderer;
import com.railwayteam.railways.content.switches.TrackSwitchRenderer;
import com.railwayteam.railways.neoforge.client.track.FullShapeDestroyEffects;
import com.railwayteam.railways.registry.CRBlockEntities;
import com.railwayteam.railways.registry.CRParticleTypes;
import com.railwayteam.railways.registry.CREntities;
import com.railwayteam.railways.registry.neoforge.CRBlockEntitiesImpl;
import com.simibubi.create.content.contraptions.actors.psi.PSIVisual;
import com.simibubi.create.content.trains.bogey.BogeyBlockEntityRenderer;
import com.simibubi.create.content.trains.bogey.BogeyBlockEntityVisual;
import com.simibubi.create.content.trains.track.TrackBlock;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.world.level.block.Block;

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
		RailwaysImpl.bus.addListener(RailwaysClientImpl::onParticleProviderRegistration);
		RailwaysImpl.bus.addListener(RailwaysClientImpl::onRendererRegistration);
		RailwaysImpl.bus.addListener(RailwaysClientImpl::onClientExtensionsRegistration);
		RailwaysImpl.bus.addListener(RailwaysClientImpl::onClientSetup);
	}

	private static void onClientExtensionsRegistration(RegisterClientExtensionsEvent event) {
		List<Block> blocks = new ArrayList<>();
		BuiltInRegistries.BLOCK.entrySet().forEach(entry -> {
			var id = entry.getKey().location();
			Block block = entry.getValue();
			if (Railways.MOD_ID.equals(id.getNamespace()) && block instanceof TrackBlock) {
				blocks.add(block);
			}
		});
		if (!blocks.isEmpty()) {
			event.registerBlock(FullShapeDestroyEffects.INSTANCE, blocks.toArray(Block[]::new));
		}
	}

	private static void onParticleProviderRegistration(RegisterParticleProvidersEvent event) {
		CRParticleTypes.registerFactories(event);
	}

	private static void onRendererRegistration(RegisterRenderers event) {
		// Registrate renderer wiring can be missed depending on init timing; explicitly bind core BE renderers.
		event.registerBlockEntityRenderer(CRBlockEntities.SEMAPHORE.get(), SemaphoreRenderer::new);
		event.registerBlockEntityRenderer(CRBlockEntities.ANDESITE_SWITCH.get(), TrackSwitchRenderer::new);
		event.registerBlockEntityRenderer(CRBlockEntities.BRASS_SWITCH.get(), TrackSwitchRenderer::new);

		// Ensure Railways bogey block entities always have a vanilla renderer bound.
		event.registerBlockEntityRenderer(CRBlockEntities.BOGEY.get(), BogeyBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(CRBlockEntities.MONO_BOGEY.get(), BogeyBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(CRBlockEntities.INVISIBLE_BOGEY.get(), BogeyBlockEntityRenderer::new);
		event.registerBlockEntityRenderer(CRBlockEntities.INVISIBLE_MONO_BOGEY.get(), BogeyBlockEntityRenderer::new);

		// Ponder renders entities in an isolated world; ensure our entity renderers are always registered.
		event.registerEntityRenderer(CREntities.CONDUCTOR.get(), ConductorRenderer::new);
		event.registerEntityRenderer(CREntities.CART_BLOCK.get(), ctx -> new MinecartRenderer<>(ctx, ModelLayers.MINECART));
		event.registerEntityRenderer(CREntities.CART_JUKEBOX.get(), ctx -> new MinecartRenderer<>(ctx, ModelLayers.MINECART));
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		// Flywheel visuals: explicitly register visualizers for Railways bogey block entities.
		event.enqueueWork(() -> {
			var visualizer = new SimpleBlockEntityVisualizer<>(BogeyBlockEntityVisual::new, be -> true);
			VisualizerRegistry.setVisualizer(CRBlockEntities.BOGEY.get(), visualizer);
			VisualizerRegistry.setVisualizer(CRBlockEntities.MONO_BOGEY.get(), visualizer);
			VisualizerRegistry.setVisualizer(CRBlockEntities.INVISIBLE_BOGEY.get(), visualizer);
			VisualizerRegistry.setVisualizer(CRBlockEntities.INVISIBLE_MONO_BOGEY.get(), visualizer);

			// Portable Fuel Interface uses Create's PSI visual path. If a visualizer isn't registered, Create's
			// PortableStorageInterfaceRenderer will early-return under visualization, making it invisible.
			var psiVisualizer = new SimpleBlockEntityVisualizer<PortableFuelInterfaceBlockEntity>(
					(visualizationContext, be, partialTick) -> new PSIVisual(visualizationContext, be, partialTick),
					be -> true
			);
			VisualizerRegistry.setVisualizer(CRBlockEntitiesImpl.PORTABLE_FUEL_INTERFACE.get(), psiVisualizer);
		});
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
