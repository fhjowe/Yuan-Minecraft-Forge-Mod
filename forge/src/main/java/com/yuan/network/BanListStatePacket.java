package com.yuan.network;

import com.yuan.data.YuanBanClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class BanListStatePacket {
    private final List<UUID> session;
    private final List<UUID> persistent;

    public BanListStatePacket(Collection<UUID> session, Collection<UUID> persistent) {
        this.session = session.stream().limit(4096).toList();
        this.persistent = persistent.stream().limit(4096).toList();
    }

    public BanListStatePacket(FriendlyByteBuf buf) {
        session = readList(buf);
        persistent = readList(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        writeList(buf, session);
        writeList(buf, persistent);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> YuanBanClientState.update(session, persistent));
        ctx.get().setPacketHandled(true);
    }

    private static List<UUID> readList(FriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 4096);
        List<UUID> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) entries.add(buf.readUUID());
        return entries;
    }

    private static void writeList(FriendlyByteBuf buf, List<UUID> entries) {
        buf.writeVarInt(entries.size());
        for (UUID id : entries) buf.writeUUID(id);
    }
}
