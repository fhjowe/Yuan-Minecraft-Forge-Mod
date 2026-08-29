package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class TimestopTextureAtlasMixin {
    @Inject(method = "cycleAnimationFrames", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopFreezeLiquidAnimation(CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeFluids())
                || YuanTimeRewindClient.isActive()) {
            ci.cancel();
        }
    }
}
