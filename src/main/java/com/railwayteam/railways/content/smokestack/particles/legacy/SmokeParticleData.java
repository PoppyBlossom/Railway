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

package com.railwayteam.railways.content.smokestack.particles.legacy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.railwayteam.railways.registry.CRParticleTypes;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;

public class SmokeParticleData implements ParticleOptions, ICustomParticleDataWithSprite<SmokeParticleData> {

	public static final MapCodec<SmokeParticleData> CODEC = RecordCodecBuilder.mapCodec(i -> i
		.group(Codec.BOOL.fieldOf("stationary")
			.forGetter(p -> p.stationary),
			Codec.FLOAT.fieldOf("red")
				.forGetter(p -> p.red),
			Codec.FLOAT.fieldOf("green")
				.forGetter(p -> p.green),
			Codec.FLOAT.fieldOf("blue")
				.forGetter(p -> p.blue))
		.apply(i, SmokeParticleData::new));

	// Legacy 1.20.x Deserializer removed in 1.21; use MapCodec and StreamCodec instead

	public static final StreamCodec<FriendlyByteBuf, SmokeParticleData> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public void encode(FriendlyByteBuf buffer, SmokeParticleData data) {
			buffer.writeBoolean(data.stationary);
			buffer.writeFloat(data.red);
			buffer.writeFloat(data.green);
			buffer.writeFloat(data.blue);
		}

		@Override
		public SmokeParticleData decode(FriendlyByteBuf buffer) {
			return new SmokeParticleData(buffer.readBoolean(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		}
	};

	boolean stationary;
	float red;
	float green;
	float blue;

	public SmokeParticleData() {
		this(false);
	}

	public SmokeParticleData(float red, float green, float blue) {
		this(false, red, green, blue);
	}

	public SmokeParticleData(boolean stationary) {
		this(stationary, stationary ? 0.3f : 0.1f);
	}

	public SmokeParticleData(boolean stationary, float brightness) {
		this(stationary, brightness, brightness, brightness);
	}

	public SmokeParticleData(boolean stationary, float red, float green, float blue) {
		this.stationary = stationary;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public ParticleType<?> getType() {
		return CRParticleTypes.SMOKE.get();
	}

	public void writeToNetwork(FriendlyByteBuf buffer) {
		buffer.writeBoolean(stationary);
		buffer.writeFloat(red);
		buffer.writeFloat(green);
		buffer.writeFloat(blue);
	}

	public String writeToString() {
		return String.format(Locale.ROOT, "%s %b %f %f %f", CRParticleTypes.SMOKE.parameter(), stationary, red, green, blue);
	}

	// No Deserializer in 1.21

	@Override
	public MapCodec<SmokeParticleData> getCodec(ParticleType<SmokeParticleData> type) {
		return CODEC;
	}

	@Override
	public StreamCodec<FriendlyByteBuf, SmokeParticleData> getStreamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public ParticleEngine.SpriteParticleRegistration<SmokeParticleData> getMetaFactory() {
		return SmokeParticle.Factory::new;
	}
}