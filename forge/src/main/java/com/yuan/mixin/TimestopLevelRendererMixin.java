package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class TimestopLevelRendererMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopLevelRendererTick(CallbackInfo ci) {
        if (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickRain", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopTickRain(Camera camera, CallbackInfo ci) {
        if (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) {
            ci.cancel();
        }
    }
}
