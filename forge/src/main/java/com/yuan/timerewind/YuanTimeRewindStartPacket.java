package com.yuan.timerewind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class YuanTimeRewindStartPacket {
    private final int playerId;
    private final int cameraMode;
    private final boolean freeCamRestorePosition;
    private final boolean positionRewind;
    private final int positionMode;
    private final float playbackSeconds;
    private final boolean freezeOthers;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final Map<ResourceKey<Level>, List<YuanTimeRewindRestorer.RetreatTarget>> retreatTargets;

    public YuanTimeRewindStartPacket(int playerId, int cameraMode, boolean freeCamRestorePosition,
                                     boolean positionRewind, int positionMode, float playbackSeconds,
                                     boolean freezeOthers, double targetX, double targetY, double targetZ,
                                     Map<ResourceKey<Level>, List<YuanTimeRewindRestorer.RetreatTarget>> retreatTargets) {
        this.playerId = playerId;
        this.cameraMode = cameraMode;
        this.freeCamRestorePosition = freeCamRestorePosition;
        this.positionRewind = positionRewind;
        this.positionMode = positionMode;
        this.playbackSeconds = playbackSeconds;
        this.freezeOthers = freezeOthers;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.retreatTargets = retreatTargets == null ? Map.of() : retreatTargets;
    }

    public int playerId() {
        return playerId;
    }

    public int cameraMode() {
        return cameraMode;
    }

    public boolean freeCamRestorePosition() {
        return freeCamRestorePosition;
    }

    public boolean positionRewind() {
        return positionRewind;
    }

    public int positionMode() {
        return positionMode;
    }

    public float playbackSeconds() {
        return playbackSeconds;
    }

    public boolean freezeOthers() {
        return freezeOthers;
    }

    public double targetX() {
        return targetX;
    }

    public double targetY() {
        return targetY;
    }

    public double targetZ() {
        return targetZ;
    }

    public Map<ResourceKey<Level>, List<YuanTimeRewindRestorer.RetreatTarget>> retreatTargets() {
        return retreatTargets;
    }

    public static void encode(YuanTimeRewindStartPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.playerId);
        buffer.writeInt(message.cameraMode);
        buffer.writeBoolean(message.freeCamRestorePosition);
        buffer.writeBoolean(message.positionRewind);
        buffer.writeInt(message.positionMode);
        buffer.writeFloat(message.playbackSeconds);
        buffer.writeBoolean(message.freezeOthers);
        buffer.writeDouble(message.targetX);
        buffer.writeDouble(message.targetY);
        buffer.writeDouble(message.targetZ);
        buffer.writeInt(message.retreatTargets.size());
        for (Map.Entry<ResourceKey<Level>, List<YuanTimeRewindRestorer.RetreatTarget>> entry
                : message.retreatTargets.entrySet()) {
            buffer.writeResourceLocation(entry.getKey().location());
            List<YuanTimeRewindRestorer.RetreatTarget> list = entry.getValue();
            buffer.writeInt(list.size());
            for (YuanTimeRewindRestorer.RetreatTarget t : list) {
                buffer.writeUUID(t.uuid());
                buffer.writeDouble(t.x());
                buffer.writeDouble(t.y());
                buffer.writeDouble(t.z());
                buffer.writeFloat(t.yRot());
                buffer.writeFloat(t.xRot());
            }
        }
    }

    public static YuanTimeRewindStartPacket decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readInt();
        int cameraMode = buffer.readInt();
        boolean freeCamRestorePosition = buffer.readBoolean();
        boolean positionRewind = buffer.readBoolean();
        int positionMode = buffer.readInt();
        float playbackSeconds = buffer.readFloat();
        boolean freezeOthers = buffer.readBoolean();
        double targetX = buffer.readDouble();
        double targetY = buffer.readDouble();
        double targetZ = buffer.readDouble();
        Map<ResourceKey<Level>, List<YuanTimeRewindRestorer.RetreatTarget>> retreatTargets = new HashMap<>();
        int dimCount = buffer.readInt();
        for (int i = 0; i < dimCount; i++) {
            ResourceLocation dimLoc = buffer.readResourceLocation();
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, dimLoc);
            int count = buffer.readInt();
            List<YuanTimeRewindRestorer.RetreatTarget> list = new ArrayList<>(count);
            for (int j = 0; j < count; j++) {
                UUID uuid = buffer.readUUID();
                double x = buffer.readDouble();
                double y = buffer.readDouble();
                double z = buffer.readDouble();
                float yRot = buffer.readFloat();
                float xRot = buffer.readFloat();
                list.add(new YuanTimeRewindRestorer.RetreatTarget(uuid, x, y, z, yRot, xRot));
            }
            retreatTargets.put(dim, list);
        }
        return new YuanTimeRewindStartPacket(playerId, cameraMode, freeCamRestorePosition,
                positionRewind, positionMode, playbackSeconds, freezeOthers,
                targetX, targetY, targetZ, retreatTargets);
    }

    public static void handle(YuanTimeRewindStartPacket message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> YuanTimeRewindClient.onStart(
                message.playerId,
                message.cameraMode,
                message.freeCamRestorePosition,
                message.positionRewind,
                message.positionMode,
                message.playbackSeconds,
                message.freezeOthers,
                message.targetX,
                message.targetY,
                message.targetZ,
                message.retreatTargets));
        context.get().setPacketHandled(true);
    }
}
