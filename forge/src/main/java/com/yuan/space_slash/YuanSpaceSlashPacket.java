package com.yuan.space_slash;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

public class YuanSpaceSlashPacket {
    private final int entityId;
    private final int seed;
    private final float yaw;
    private final float pitch;
    private final float roll;
    private final double hitX;
    private final double hitY;
    private final double hitZ;

    public YuanSpaceSlashPacket(int entityId, int seed, float yaw, float pitch, float roll,
                                double hitX, double hitY, double hitZ) {
        this.entityId = entityId;
        this.seed = seed;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
    }

    public static void encode(YuanSpaceSlashPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.entityId);
        buffer.writeInt(message.seed);
        buffer.writeFloat(message.yaw);
        buffer.writeFloat(message.pitch);
        buffer.writeFloat(message.roll);
        buffer.writeDouble(message.hitX);
        buffer.writeDouble(message.hitY);
        buffer.writeDouble(message.hitZ);
    }

    public static YuanSpaceSlashPacket decode(FriendlyByteBuf buffer) {
        return new YuanSpaceSlashPacket(
                buffer.readInt(), buffer.readInt(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(YuanSpaceSlashPacket message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (message == null) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Entity target = mc.level.getEntity(message.entityId);
                YuanSpaceSlashRender.spawn(target, message.seed,
                        message.yaw, message.pitch, message.roll,
                        message.hitX, message.hitY, message.hitZ);
            }
        });
        context.get().setPacketHandled(true);
    }

    public int getEntityId() {
        return entityId;
    }

    public int getSeed() {
        return seed;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public double getHitX() {
        return hitX;
    }

    public double getHitY() {
        return hitY;
    }

    public double getHitZ() {
        return hitZ;
    }
}
