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

package com.railwayteam.railways.content.smokestack.particles.puffs;

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
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public abstract class PuffSmokeParticleData<T extends PuffSmokeParticleData<T>> implements ParticleOptions, ICustomParticleDataWithSprite<T> {
	
	@FunctionalInterface
	protected interface Constructor<T extends PuffSmokeParticleData<T>> {
		@Contract("_, _, _, _ -> new")
		T create(boolean stationary, float red, float green, float blue);
	}

	protected static <T extends PuffSmokeParticleData<T>> MapCodec<T> makeCodec(Constructor<T> constructor) {
		return RecordCodecBuilder.mapCodec(i -> i
			.group(Codec.BOOL.fieldOf("stationary")
					.forGetter(p -> p.stationary),
				Codec.FLOAT.fieldOf("red") // -1, -1, -1 indicates un-dyed
					.forGetter(p -> p.red),
				Codec.FLOAT.fieldOf("green")
					.forGetter(p -> p.green),
				Codec.FLOAT.fieldOf("blue")
					.forGetter(p -> p.blue))
			.apply(i, constructor::create));
	}

	// Legacy 1.20.x command/network deserializer removed in 1.21; use MapCodec and StreamCodec instead
	
	boolean stationary;
	float red;
	float green;
	float blue;

	protected PuffSmokeParticleData() {
		this(false);
	}

	protected PuffSmokeParticleData(float red, float green, float blue) {
		this(false, red, green, blue);
	}

	protected PuffSmokeParticleData(boolean stationary) {
		this(stationary, -1);
	}

	protected PuffSmokeParticleData(boolean stationary, float brightness) {
		this(stationary, brightness, brightness, brightness);
	}

	protected PuffSmokeParticleData(boolean stationary, float red, float green, float blue) {
		this.stationary = stationary;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	protected abstract @NotNull CRParticleTypes getParticleType();

	public @NotNull ParticleType<?> getType() {
		return getParticleType().get();
	}

	public void writeToNetwork(FriendlyByteBuf buffer) {
		buffer.writeBoolean(stationary);
		buffer.writeFloat(red);
		buffer.writeFloat(green);
		buffer.writeFloat(blue);
	}

	public @NotNull String writeToString() {
		return String.format(Locale.ROOT, "%s %b %f %f %f", getParticleType().parameter(), stationary, red, green, blue);
	}

	// Deserializer removed in 1.21

	@Override
	public abstract MapCodec<T> getCodec(ParticleType<T> type);

	public abstract StreamCodec<FriendlyByteBuf, T> getStreamCodec();

	@Override
	public abstract ParticleEngine.SpriteParticleRegistration<T> getMetaFactory();

	public abstract float getQuadSize();

	public static PuffSmokeParticleData<?> create(boolean small, boolean stationary, @NotNull DyeColor color) {
		float idx = -(3+color.getId());
		return create(small, stationary, idx, idx, idx);
	}

	public static PuffSmokeParticleData<?> create(boolean small, boolean stationary, float red, float green, float blue) {
		if (small) {
			return new Small(stationary, red, green, blue);
		} else {
			return new Medium(stationary, red, green, blue);
		}
	}

	public static PuffSmokeParticleData<?> create(boolean small, boolean stationary) {
		if (small) {
			return new Small(stationary);
		} else {
			return new Medium(stationary);
		}
	}

	public static class Small extends PuffSmokeParticleData<Small> {
		public static final MapCodec<Small> CODEC = makeCodec(Small::new);


		public static final StreamCodec<FriendlyByteBuf, Small> STREAM_CODEC = new StreamCodec<>() {
			@Override
			public void encode(FriendlyByteBuf buffer, Small data) {
				buffer.writeBoolean(data.stationary);
				buffer.writeFloat(data.red);
				buffer.writeFloat(data.green);
				buffer.writeFloat(data.blue);
			}

			@Override
			public Small decode(FriendlyByteBuf buffer) {
				return new Small(buffer.readBoolean(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
			}
		};

		public Small() {}

		public Small(float red, float green, float blue) {
			super(red, green, blue);
		}

		public Small(boolean stationary) {
			super(stationary);
		}

		public Small(boolean stationary, float brightness) {
			super(stationary, brightness);
		}

		public Small(boolean stationary, float red, float green, float blue) {
			super(stationary, red, green, blue);
		}

		@Override
		protected @NotNull CRParticleTypes getParticleType() {
			return CRParticleTypes.SMOKE_PUFF_SMALL;
		}

		// No Deserializer in 1.21

		@Override
		public MapCodec<Small> getCodec(ParticleType<Small> type) {
			return CODEC;
		}

		@Override
		public StreamCodec<FriendlyByteBuf, Small> getStreamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public ParticleEngine.SpriteParticleRegistration<Small> getMetaFactory() {
			return PuffSmokeParticle.Factory::new;
		}

		@Override
		public float getQuadSize() {
			return 0.5f;
		}
	}

	public static class Medium extends PuffSmokeParticleData<Medium> {
		public static final MapCodec<Medium> CODEC = makeCodec(Medium::new);


		public static final StreamCodec<FriendlyByteBuf, Medium> STREAM_CODEC = new StreamCodec<>() {
			@Override
			public void encode(FriendlyByteBuf buffer, Medium data) {
				buffer.writeBoolean(data.stationary);
				buffer.writeFloat(data.red);
				buffer.writeFloat(data.green);
				buffer.writeFloat(data.blue);
			}

			@Override
			public Medium decode(FriendlyByteBuf buffer) {
				return new Medium(buffer.readBoolean(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
			}
		};

		public Medium() {}

		public Medium(float red, float green, float blue) {
			super(red, green, blue);
		}

		public Medium(boolean stationary) {
			super(stationary);
		}

		public Medium(boolean stationary, float brightness) {
			super(stationary, brightness);
		}

		public Medium(boolean stationary, float red, float green, float blue) {
			super(stationary, red, green, blue);
		}

		@Override
		protected @NotNull CRParticleTypes getParticleType() {
			return CRParticleTypes.SMOKE_PUFF_MEDIUM;
		}

		// No Deserializer in 1.21

		@Override
		public MapCodec<Medium> getCodec(ParticleType<Medium> type) {
			return CODEC;
		}

		@Override
		public StreamCodec<FriendlyByteBuf, Medium> getStreamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public ParticleEngine.SpriteParticleRegistration<Medium> getMetaFactory() {
			return PuffSmokeParticle.Factory::new;
		}

		@Override
		public float getQuadSize() {
			return 1;
		}
	}
}