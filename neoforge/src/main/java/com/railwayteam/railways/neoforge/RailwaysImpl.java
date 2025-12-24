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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.CommandDispatcher;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.config.neoforge.CRConfigsImpl;
import com.railwayteam.railways.content.fuel.tank.FuelTankBlock;
import com.railwayteam.railways.multiloader.Env;
import com.railwayteam.railways.registry.neoforge.CRBlockEntitiesImpl;
import com.railwayteam.railways.registry.neoforge.CRBlocksImpl;
import com.railwayteam.railways.registry.neoforge.CRCreativeModeTabsImpl;
import com.railwayteam.railways.registry.neoforge.CREntityAttributesImpl;
import com.railwayteam.railways.registry.neoforge.CRMountedStorageTypesImpl;
import com.railwayteam.railways.registry.neoforge.CRParticleTypesParticleEntryImpl;
import com.railwayteam.railways.base.data.CRTagGen;
import com.railwayteam.railways.base.data.lang.CRLangGen;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.CreativeModeTabModifier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(Railways.MOD_ID)
@EventBusSubscriber
public class RailwaysImpl {	
	static IEventBus bus;

	public RailwaysImpl(IEventBus modEventBus, ModContainer modContainer) {
		bus = modEventBus;
		CRCreativeModeTabsImpl.register(RailwaysImpl.bus);
		// Ensure mob attributes exist even if Registrate attribute wiring is missed.
		modEventBus.addListener(CREntityAttributesImpl::registerAttributes);
		Railways.init();
		CRConfigsImpl.register(modContainer);
		CRParticleTypesParticleEntryImpl.register(bus);
		
		// Register network payloads for NeoForge 1.21.1+
		modEventBus.addListener(RailwaysNetworking::registerPayloads);
		
		//noinspection Convert2MethodRef
		Env.CLIENT.runIfCurrent(() -> () -> RailwaysClientImpl.init());
	}

	public static void finalizeRegistrate() {
		CreateRegistrate registrate = Railways.registrate();
		
		// Register data generators BEFORE suppressing tab modifiers and registering event listeners
		// This must be done before datagen event fires
		registrate.addDataGenerator(ProviderType.BLOCK_TAGS, CRTagGen::generateBlockTags);
		registrate.addDataGenerator(ProviderType.ITEM_TAGS, CRTagGen::generateItemTags);
		registrate.addDataGenerator(ProviderType.LANG, CRLangGen::generate);
		
		suppressRegistrateTabModifiers(registrate);
		registrate.registerEventListeners(bus);
	}

	/**
	 * Suppresses Registrate's automatic creative tab modifier registration to prevent duplicate tab content.
	 * <p>
	 * This uses reflection to replace the internal creativeModeTabModifiers multimap with a no-op implementation.
	 * This is necessary because Registrate (version MC1.21-1.3.0+62) automatically adds items to creative tabs
	 * via .tab() calls, which would duplicate our custom tab population logic in RegistrateDisplayItemsGenerator
	 * and cause inventory crashes (issue #70).
	 * </p>
	 *
	 * @param registrate the registrate instance to modify
	 */
	private static void suppressRegistrateTabModifiers(CreateRegistrate registrate) {
		try {
			Field field = AbstractRegistrate.class.getDeclaredField("creativeModeTabModifiers");
			field.setAccessible(true);
			Object value = field.get(registrate);
			if (value instanceof Multimap<?, ?> multimap) {
				multimap.clear();
			}
			field.set(registrate, new NoOpCreativeTabMultimap());
		} catch (InaccessibleObjectException | SecurityException e) {
			// Module access restriction - provide guidance on JVM flags
			throw new IllegalStateException(
				"Failed to access Registrate creative tab modifiers field. " +
				"If running on Java 9+, you may need to add JVM flag: " +
				"--add-opens com.tterrag.registrate/com.tterrag.registrate=ALL-UNNAMED", e);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to clear Registrate creative tab modifiers", e);
		}
	}

	/**
	 * A no-op multimap that drops all future creative tab modifier registrations so Registrate cannot duplicate our tab content.
	 * <p>
	 * This class intentionally maintains an internal delegate multimap but all mutation methods (put, putAll, etc.)
	 * return false without actually storing data. This creates a discrepancy where read operations (get, size, containsKey)
	 * will always return empty results even if callers attempt to store data. This is the intended behavior to suppress
	 * Registrate's automatic tab registration while maintaining a valid Multimap interface.
	 * </p>
	 */
	private static final class NoOpCreativeTabMultimap extends ForwardingMultimap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> {
		private final Multimap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> delegate = ArrayListMultimap.create();

		@Override
		protected Multimap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> delegate() {
			return delegate;
		}

		@Override
		public boolean put(ResourceKey<CreativeModeTab> key, Consumer<CreativeModeTabModifier> value) {
			return false;
		}

		@Override
		public boolean putAll(ResourceKey<CreativeModeTab> key, Iterable<? extends Consumer<CreativeModeTabModifier>> values) {
			return false;
		}

		@Override
		public boolean putAll(Multimap<? extends ResourceKey<CreativeModeTab>, ? extends Consumer<CreativeModeTabModifier>> multimap) {
			return false;
		}

		@Override
		public Collection<Consumer<CreativeModeTabModifier>> replaceValues(ResourceKey<CreativeModeTab> key, Iterable<? extends Consumer<CreativeModeTabModifier>> values) {
			Collection<Consumer<CreativeModeTabModifier>> removed = delegate().get(key);
			delegate().removeAll(key);
			return removed;
		}
	}

	private static final Set<BiConsumer<CommandDispatcher<CommandSourceStack>, Boolean>> commandConsumers = new HashSet<>();

	public static void registerCommands(BiConsumer<CommandDispatcher<CommandSourceStack>, Boolean> consumer) {
		commandConsumers.add(consumer);
	}

	@SubscribeEvent
	public static void onCommandRegistration(RegisterCommandsEvent event) {
		CommandSelection selection = event.getCommandSelection();
		boolean dedicated = selection == CommandSelection.ALL || selection == CommandSelection.DEDICATED;
		commandConsumers.forEach(consumer -> consumer.accept(event.getDispatcher(), dedicated));
	}

	public static void platformBasedRegistration() {
		BlockMovementChecks.registerAttachedCheck((BlockState state, Level world, BlockPos pos, Direction direction) -> {
			if (state.getBlock() instanceof FuelTankBlock && ConnectivityHandler.isConnected(world, pos, pos.relative(direction)))
				return CheckResult.SUCCESS;
			return CheckResult.PASS;
		});

		CRMountedStorageTypesImpl.init();
		CRBlocksImpl.init();
		CRBlockEntitiesImpl.init();
	}
}
