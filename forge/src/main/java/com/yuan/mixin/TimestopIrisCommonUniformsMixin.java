package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.irisshaders.iris.uniforms.CommonUniforms", remap = false)
public abstract class TimestopIrisCommonUniformsMixin {
    @Inject(method = "addCommonUniforms", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void yuanTimestopIrisCommonUniforms(CallbackInfo ci) {
        if (YuanTimeStop.get()) {
            ci.cancel();
        }
    }
}
