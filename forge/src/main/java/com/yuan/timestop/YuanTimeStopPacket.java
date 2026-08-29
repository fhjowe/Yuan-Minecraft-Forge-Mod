package com.yuan.timestop;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class YuanTimeStopPacket {
    private final boolean paused;
    private final int id;
    private final boolean freezeSelf;
    private final boolean freezeEntities;
    private final boolean freezeBlocks;
    private final boolean freezeFluids;
    private final boolean freezeBossAI;
    private final float stopRadius;
    private final double wielderX;
    private final double wielderY;
    private final double wielderZ;

    public YuanTimeStopPacket(boolean paused, int id,
                              boolean freezeSelf, boolean freezeEntities, boolean freezeBlocks,
                              boolean freezeFluids, boolean freezeBossAI, float stopRadius,
                              double wielderX, double wielderY, double wielderZ) {
        this.paused = paused;
        this.id = id;
        this.freezeSelf = freezeSelf;
        this.freezeEntities = freezeEntities;
        this.freezeBlocks = freezeBlocks;
        this.freezeFluids = freezeFluids;
        this.freezeBossAI = freezeBossAI;
        this.stopRadius = stopRadius;
        this.wielderX = wielderX;
        this.wielderY = wielderY;
        this.wielderZ = wielderZ;
    }

    public static void encode(YuanTimeStopPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.paused);
        buffer.writeInt(message.id);
        buffer.writeBoolean(message.freezeSelf);
        buffer.writeBoolean(message.freezeEntities);
        buffer.writeBoolean(message.freezeBlocks);
        buffer.writeBoolean(message.freezeFluids);
        buffer.writeBoolean(message.freezeBossAI);
        buffer.writeFloat(message.stopRadius);
        buffer.writeDouble(message.wielderX);
        buffer.writeDouble(message.wielderY);
        buffer.writeDouble(message.wielderZ);
    }

    public static YuanTimeStopPacket decode(FriendlyByteBuf buffer) {
        return new YuanTimeStopPacket(
                buffer.readBoolean(), buffer.readInt(),
                buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(),
                buffer.readBoolean(), buffer.readBoolean(), buffer.readFloat(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(YuanTimeStopPacket message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (message == null) {
                return;
            }
            YuanTimeStopServerState.setFreezeSelf(message.freezeSelf);
            YuanTimeStopServerState.setFreezeEntities(message.freezeEntities);
            YuanTimeStopServerState.setFreezeBlocks(message.freezeBlocks);
            YuanTimeStopServerState.setFreezeFluids(message.freezeFluids);
            YuanTimeStopServerState.setFreezeBossAI(message.freezeBossAI);
            YuanTimeStopServerState.setStopRadius(message.stopRadius);
            YuanTimeStopServerState.setWielderPosition(message.wielderX, message.wielderY, message.wielderZ);
            boolean wasStopped = YuanTimeStop.get();
            YuanTimeStop.setIsTimeStop(message.paused);
            if (!message.paused && wasStopped && YuanTimeStop.consumeLocalEnd()) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    YuanTimeStop.playEndSound(player);
                    YuanTimeStop.spawnEndEffect(player);
                }
            }
            if (message.paused && YuanTimeStopConfig.specialShader) {
                YuanTimeStopShaders.timeTheWorld += 100.0f;
                Minecraft.getInstance().gameRenderer.loadEffect(YuanTimeStopShaders.effectLocation());
            } else {
                Minecraft.getInstance().gameRenderer.shutdownEffect();
            }
        });
        context.get().setPacketHandled(true);
    }

    public boolean isPaused() {
        return paused;
    }

    public int getId() {
        return id;
    }
}
