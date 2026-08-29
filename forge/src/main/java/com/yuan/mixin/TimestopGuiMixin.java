package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class TimestopGuiMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopGuiTick(boolean pause, CallbackInfo ci) {
        if (YuanTimeStopServerState.isStopped()) {
            ci.cancel();
        }
    }
}
