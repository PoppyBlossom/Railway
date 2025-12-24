package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlockEntity;
import com.railwayteam.railways.multiloader.S2CPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrackCouplerClientInfoPacket implements S2CPacket {
    final BlockPos blockPos;
    final CompoundTag infoTag;

    public TrackCouplerClientInfoPacket(TrackCouplerBlockEntity te) {
        blockPos = te.getBlockPos();
        TrackCouplerBlockEntity.ClientInfo info = te.getClientInfo();
        // Serialize using the server-side registry access
        HolderLookup.Provider lookupProvider = te.getLevel() != null ? te.getLevel().registryAccess() : null;
        infoTag = info.write(lookupProvider != null ? lookupProvider : HolderLookup.Provider.create(java.util.stream.Stream.empty()));
    }

    public TrackCouplerClientInfoPacket(FriendlyByteBuf buf) {
        blockPos = buf.readBlockPos();
        infoTag = buf.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeNbt(infoTag);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(Minecraft mc) {
        Level level = mc.level;
        if (level != null) {
            BlockEntity te = level.getBlockEntity(blockPos);
            if (te instanceof TrackCouplerBlockEntity couplerTile) {
                // Deserialize using the client-side registry access
                HolderLookup.Provider lookupProvider = level.registryAccess();
                TrackCouplerBlockEntity.ClientInfo info = new TrackCouplerBlockEntity.ClientInfo(infoTag, lookupProvider);
                couplerTile.setClientInfo(info);
            }
        }
    }
}
