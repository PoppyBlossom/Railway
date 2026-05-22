/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.registry.CRItems;
import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class RailwaysPotatoProjectileTypes {
    public static void bootstrap(BootstrapContext<PotatoCannonProjectileType> ctx) {
        // Register paint pitcher projectiles for each color
        for (var pitcher : CRItems.FILLED_PITCHERS) {
            String pitcherName = pitcher.getId().getPath();
            String projName = pitcherName.replace("_paint_pitcher", "_paint");
            
            // Register with standard paint properties: damage, reload, velocity, knockback
            register(ctx, projName, new PotatoCannonProjectileType.Builder()
                .damage(3)
                .reloadTicks(15)
                .velocity(1.25f)
                .knockback(1.5f)
                .renderTumbling()
                .onEntityHit(new AllPotatoProjectileEntityHitActions.PotionEffect(
                    net.minecraft.world.effect.MobEffects.POISON, 1, 100, false
                ))
                .addItems(pitcher.get())
                .build());
        }
    }

    private static void register(BootstrapContext<PotatoCannonProjectileType> ctx, String name, PotatoCannonProjectileType type) {
        ctx.register(
            ResourceKey.create(CreateRegistries.POTATO_PROJECTILE_TYPE, Railways.asResource(name)),
            type
        );
    }
}
