package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelChunkMixin {

    @Inject(method = "tickChunk", at = @At("HEAD"), cancellable = true)
    private void onTickChunk(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        if (YuanSwordEvents.shouldFreezeWorldSystems()) {
            ci.cancel();
        }
    }
}
