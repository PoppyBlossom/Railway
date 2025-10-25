package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.content.minecarts.MinecartJukebox;
import com.railwayteam.railways.multiloader.S2CPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class JukeboxCartPacket implements S2CPacket {
  final int id;
  final CompoundTag recordTag;

  public JukeboxCartPacket(Entity target, ItemStack disc) {
    id = target.getId();
    recordTag = (CompoundTag) disc.save(target.level().registryAccess());
  }

  public JukeboxCartPacket(FriendlyByteBuf buf) {
    id = buf.readInt();
    // 1.21: ItemStack serialization now requires RegistryFriendlyByteBuf for stream codecs; use NBT fallback
    recordTag = buf.readNbt();
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeInt(this.id);
    buffer.writeNbt(this.recordTag);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void handle(Minecraft mc) {
    Level level = mc.level;
    if (level != null) {
      Entity target = level.getEntity(this.id);
      if (target instanceof MinecartJukebox juke) {
        HolderLookup.Provider registryAccess = level.registryAccess();
        ItemStack stack = recordTag != null ? ItemStack.parseOptional(registryAccess, recordTag) : ItemStack.EMPTY;
        juke.insertRecord(stack);
      }
    }
  }
}
