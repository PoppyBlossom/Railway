package net.minecraft.core.particles;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;

/** Minimal compile-time shim for ParticleOptions and nested Deserializer.
 * The real classes are provided by Minecraft/NeoForge at runtime.
 */
public interface ParticleOptions {
    interface Deserializer<T extends ParticleOptions> {
        T fromCommand(ParticleType<T> particleTypeIn, com.mojang.brigadier.StringReader reader) throws com.mojang.brigadier.exceptions.CommandSyntaxException;
        T fromNetwork(ParticleType<T> particleTypeIn, FriendlyByteBuf buffer);
    }

    <T extends ParticleOptions> Codec<T> getCodec(ParticleType<T> type);
}
