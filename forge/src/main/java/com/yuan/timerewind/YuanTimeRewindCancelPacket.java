package com.yuan.timerewind;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Client asks the server to cancel the ongoing rewind playback early. */
public class YuanTimeRewindCancelPacket {
    public YuanTimeRewindCancelPacket() {}

    public static void encode(YuanTimeRewindCancelPacket message, FriendlyByteBuf buffer) {}

    public static YuanTimeRewindCancelPacket decode(FriendlyByteBuf buffer) {
        return new YuanTimeRewindCancelPacket();
    }

    public static void handle(YuanTimeRewindCancelPacket message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || !YuanTimeRewindServerState.isRewinding()) return;
            UUID active = YuanTimeRewindServerState.getActivePlayerUuid();
            if (active == null || !active.equals(player.getUUID())) return;
            YuanTimeRewindEvents.cancelPendingPlayback();
        });
        context.get().setPacketHandled(true);
    }
}
