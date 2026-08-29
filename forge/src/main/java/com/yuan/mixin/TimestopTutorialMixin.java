package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import net.minecraft.client.tutorial.Tutorial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Tutorial.class)
public abstract class TimestopTutorialMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopTutorialTick(CallbackInfo ci) {
        if (YuanTimeStopServerState.isStopped()) {
            ci.cancel();
        }
    }
}
