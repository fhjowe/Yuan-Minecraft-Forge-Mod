package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public abstract class TimestopMusicManagerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopMusicTick(CallbackInfo ci) {
        if (YuanTimeStopServerState.isStopped()) {
            ci.cancel();
        }
    }
}
