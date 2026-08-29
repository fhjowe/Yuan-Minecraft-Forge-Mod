package com.yuan.network;

import com.yuan.Yuan;
import com.yuan.data.YuanBanData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class BanListRequestPacket {
    public BanListRequestPacket() {}
    public BanListRequestPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.hasPermissions(2)) send(player);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void send(ServerPlayer player) {
        YuanBanData data = YuanBanData.get(player.server);
        Yuan.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new BanListStatePacket(YuanBanData.sessionEntries(), data.persistentEntries()));
    }
}
