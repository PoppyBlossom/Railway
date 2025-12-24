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

package com.railwayteam.railways.neoforge.events;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.railwayteam.railways.mixin.AccessorToolboxBlockEntity;
import com.railwayteam.railways.registry.neoforge.CRBlockEntitiesImpl;
import com.railwayteam.railways.content.fuel.LiquidFuelManager;
import com.railwayteam.railways.events.CommonEvents;
import com.railwayteam.railways.registry.CREntities;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.item.ItemStack;

@EventBusSubscriber
public class CommonEventsForge {
	@SubscribeEvent
	public static void onWorldTick(LevelTickEvent.Pre event) {
		CommonEvents.onWorldTickStart(event.getLevel());
	}

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player)
			CommonEvents.onPlayerJoin(player);
	}

	@SubscribeEvent
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		// Register block entity capability provider for FuelTank
		event.registerBlockEntity(
				Capabilities.FluidHandler.BLOCK,
				CRBlockEntitiesImpl.FUEL_TANK.get(),
				(be, side) -> be.getFluidHandler(side)
		);

		// Register block entity capability provider for PortableFuelInterface (fluid handler while engaged)
		event.registerBlockEntity(
				Capabilities.FluidHandler.BLOCK,
				CRBlockEntitiesImpl.PORTABLE_FUEL_INTERFACE.get(),
				(be, side) -> be.getFluidHandler(side)
		);

	// Register entity capability provider for Conductor item handler
	event.registerEntity(
		Capabilities.ItemHandler.ENTITY,
		CREntities.CONDUCTOR.get(),
		(entity, context) -> new ConductorItemHandler((ConductorEntity) entity)
	);
	}

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) {
		if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
			CommonEvents.onTagsUpdated();
	}

	@SubscribeEvent
	public static void addReloadListeners(AddReloadListenerEvent event) {
		event.addListener(LiquidFuelManager.ReloadListener.INSTANCE);
	}
}

/**
 * Dynamic IItemHandler that delegates to the Conductor's mounted toolbox when present,
 * and behaves as empty when not present. This avoids capability cache invalidation issues.
 */
class ConductorItemHandler implements IItemHandler {
	private final ConductorEntity conductor;

	ConductorItemHandler(ConductorEntity conductor) {
		this.conductor = conductor;
	}

	private @Nullable com.simibubi.create.content.equipment.toolbox.ToolboxInventory inv() {
		MountedToolbox tb = conductor.getToolbox();
		if (tb == null)
			return null;
		if (tb instanceof AccessorToolboxBlockEntity accessor)
			return accessor.getInventory();
		return null;
	}

	@Override
	public int getSlots() {
		var inv = inv();
		return inv != null ? inv.getSlots() : 0;
	}

	@Override
	public @NotNull ItemStack getStackInSlot(int slot) {
		var inv = inv();
		return inv != null ? inv.getStackInSlot(slot) : ItemStack.EMPTY;
	}

	@Override
	public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		var inv = inv();
		return inv != null ? inv.insertItem(slot, stack, simulate) : stack;
	}

	@Override
	public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
		var inv = inv();
		return inv != null ? inv.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		var inv = inv();
		return inv != null ? inv.getSlotLimit(slot) : 0;
	}

	@Override
	public boolean isItemValid(int slot, @NotNull ItemStack stack) {
		var inv = inv();
		return inv != null && inv.isItemValid(slot, stack);
	}
}
