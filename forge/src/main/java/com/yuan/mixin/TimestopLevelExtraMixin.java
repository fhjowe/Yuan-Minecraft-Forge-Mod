package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class TimestopLevelExtraMixin {
    @Inject(method = "tickBlockEntities", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopTickBlockEntities(CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeBlocks())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }

    @Inject(method = "updateSkyBrightness", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopUpdateSkyBrightness(CallbackInfo ci) {
        if (YuanTimeStopServerState.isStopped() || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }
}
