package com.yuan.network;

import com.yuan.data.YuanBanData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class BanRemovePacket {
    private final boolean all;
    private final UUID id;

    public BanRemovePacket(UUID id) { this(false, id); }
    public static BanRemovePacket all() { return new BanRemovePacket(true, null); }
    private BanRemovePacket(boolean all, UUID id) { this.all = all; this.id = id; }

    public BanRemovePacket(FriendlyByteBuf buf) {
        all = buf.readBoolean();
        id = all ? null : buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(all);
        if (!all) buf.writeUUID(id);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.hasPermissions(2)) return;
            if (all) {
                YuanBanData.clearSession();
                YuanBanData.get(player.server).clearPersistent();
            } else if (id != null) {
                YuanBanData.remove(player.server, id);
            }
            BanListRequestPacket.send(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
