package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import net.minecraft.server.ServerFunctionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerFunctionManager.class)
public abstract class TimestopFunctionManagerMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopFunctionTick(CallbackInfo ci) {
        if (YuanTimeStopServerState.isStopped() || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }
}
