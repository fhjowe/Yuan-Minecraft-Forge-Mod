package com.yuan.network;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TimeStopStatePacket {
    private final boolean enabled;
    private final UUID wielder;
    private final boolean full;
    private final String dimension;
    private final double x;
    private final double y;
    private final double z;
    private final double range;

    public TimeStopStatePacket(boolean enabled, UUID wielder, boolean full, String dimension,
                               double x, double y, double z, double range) {
        this.enabled = enabled;
        this.wielder = wielder;
        this.full = full;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.range = range;
    }

    public TimeStopStatePacket(FriendlyByteBuf buf) {
        enabled = buf.readBoolean();
        wielder = buf.readBoolean() ? buf.readUUID() : null;
        full = buf.readBoolean();
        dimension = buf.readUtf(256);
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        range = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(enabled);
        buf.writeBoolean(wielder != null);
        if (wielder != null) buf.writeUUID(wielder);
        buf.writeBoolean(full);
        buf.writeUtf(dimension, 256);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(range);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> YuanSwordEvents.applyClientTimeState(
                enabled, wielder, full, dimension, x, y, z, range));
        ctx.get().setPacketHandled(true);
    }
}
