package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MouseHandler.class)
public abstract class TimestopMouseHandlerMixin {
    @Inject(method = "isMouseGrabbed", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopIsMouseGrabbed(CallbackInfoReturnable<Boolean> cir) {
        if (YuanTimeStop.shouldBlockPlayerInput()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopGrabMouse(CallbackInfo ci) {
        if (YuanTimeStop.shouldBlockPlayerInput()) {
            ci.cancel();
        }
    }
}
