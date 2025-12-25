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

package com.railwayteam.railways.registry.neoforge;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleData;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CRParticleTypesParticleEntryImpl {
    private static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Railways.MOD_ID);
    public static void register(String id, Supplier<ParticleType<?>> supplier) {
        REGISTER.register(id, supplier);
    }

    public static void register(IEventBus modEventBus) {
        CRParticleTypes.init();
        REGISTER.register(modEventBus);
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends ParticleOptions> void registerFactory(ParticleType<T> object, RegisterParticleProvidersEvent event, ICustomParticleData<T> customParticleData) {
        if (customParticleData instanceof ICustomParticleDataWithSprite<T> withSprite) {
            // In Minecraft 1.21, we need to unwrap the SpriteParticleRegistration
            var metaFactory = withSprite.getMetaFactory();
            event.registerSpriteSet(object, spriteSet -> metaFactory.create(spriteSet));
        } else {
            event.registerSpecial(object, customParticleData.getFactory());
        }
    }
}
