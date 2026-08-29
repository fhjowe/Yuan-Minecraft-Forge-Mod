package com.yuan.timerewind;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class YuanTimeRewindEndPacket {
    private final int playerId;

    public YuanTimeRewindEndPacket(int playerId) {
        this.playerId = playerId;
    }

    public int playerId() {
        return playerId;
    }

    public static void encode(YuanTimeRewindEndPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.playerId);
    }

    public static YuanTimeRewindEndPacket decode(FriendlyByteBuf buffer) {
        return new YuanTimeRewindEndPacket(buffer.readInt());
    }

    public static void handle(YuanTimeRewindEndPacket message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(YuanTimeRewindClient::onEnd);
        context.get().setPacketHandled(true);
    }
}
