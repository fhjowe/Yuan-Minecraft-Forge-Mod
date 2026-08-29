package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class TimestopClientPacketListenerMixin {
    @Inject(method = "handleMoveEntity", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopMove(ClientboundMoveEntityPacket packet, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        Entity entity = level == null ? null : packet.getEntity(level);
        if (YuanTimeRewindClient.shouldFreezeEntity(entity)
                || (YuanTimeStop.get() && entity != null && YuanTimeStop.shouldFreezeEntity(entity))) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        Entity entity = level == null ? null : level.getEntity(packet.getId());
        if (YuanTimeRewindClient.shouldFreezeEntity(entity)
                || (YuanTimeStop.get() && entity != null && YuanTimeStop.shouldFreezeEntity(entity))) {
            ci.cancel();
        }
    }

    @Inject(method = "handleTeleportEntity", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopTeleport(ClientboundTeleportEntityPacket packet, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        Entity entity = level == null ? null : level.getEntity(packet.getId());
        if (YuanTimeRewindClient.shouldFreezeEntity(entity)
                || (YuanTimeStop.get() && entity != null && YuanTimeStop.shouldFreezeEntity(entity))) {
            ci.cancel();
        }
    }

    @Inject(method = "handleRotateMob", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopRotateHead(ClientboundRotateHeadPacket packet, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        Entity entity = level == null ? null : packet.getEntity(level);
        if (YuanTimeRewindClient.shouldFreezeEntity(entity)
                || (YuanTimeStop.get() && entity != null && YuanTimeStop.shouldFreezeEntity(entity))) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetEntityData", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopEntityData(ClientboundSetEntityDataPacket packet, CallbackInfo ci) {
        Level level = Minecraft.getInstance().level;
        Entity entity = level == null ? null : level.getEntity(packet.id());
        if (YuanTimeRewindClient.shouldFreezeEntity(entity)
                || (YuanTimeStop.get() && entity != null && YuanTimeStop.shouldFreezeEntity(entity))) {
            ci.cancel();
        }
    }
}
