package com.yuan.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class ConfigSyncAckPacket {
    private final long requestId;
    private final boolean success;
    private final CompoundTag config;
    private final int corrections;
    private final String message;

    public ConfigSyncAckPacket(long requestId, boolean success, CompoundTag config, int corrections, String message) {
        this.requestId = requestId;
        this.success = success;
        this.config = config == null ? new CompoundTag() : config.copy();
        this.corrections = corrections;
        this.message = message == null ? "" : message;
    }

    public ConfigSyncAckPacket(FriendlyByteBuf buf) {
        requestId = buf.readVarLong();
        success = buf.readBoolean();
        CompoundTag read = buf.readNbt();
        config = read == null ? new CompoundTag() : read;
        corrections = buf.readVarInt();
        message = buf.readUtf(256);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(requestId);
        buf.writeBoolean(success);
        buf.writeNbt(config);
        buf.writeVarInt(corrections);
        buf.writeUtf(message, 256);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.yuan.client.gui.YuanConfigAckClientHandler.handle(
                        requestId, success, config, corrections, message)));
        ctx.get().setPacketHandled(true);
    }
}
