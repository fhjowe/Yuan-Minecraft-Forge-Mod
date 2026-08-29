package com.yuan.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.yuan.timestop.YuanTimeStop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputConstants.class)
public abstract class TimestopInputConstantsMixin {
    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    private static void yuanTimestopIsKeyDown(long window, int key, CallbackInfoReturnable<Boolean> cir) {
        if (YuanTimeStop.shouldBlockPlayerInput()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateRawMouseInput", at = @At("HEAD"), cancellable = true)
    private static void yuanTimestopRawMouseInput(long window, boolean grab, CallbackInfo ci) {
        if (YuanTimeStop.shouldBlockPlayerInput()) {
            ci.cancel();
        }
    }
}
