package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ServerChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.class)
public abstract class TimestopServerChunkCacheMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopServerChunkCacheTick(BooleanSupplier hasWork, boolean keepAlive, CallbackInfo ci) {
        // Only freeze the chunk-cache tick during timestop. During rewind playback it must keep
        // running: ChunkHolder.blockChanged only records changed blocks and the actual
        // ClientboundBlockUpdatePacket is broadcast from ChunkMap.tick (inside this tick), so
        // freezing it here would defer every block revert until playback ends (blocks snap).
        // Entity retreat positions no longer depend on this channel (client-driven animation).
        if (YuanTimeStopServerState.isStopped()) {
            ci.cancel();
        }
    }
}
