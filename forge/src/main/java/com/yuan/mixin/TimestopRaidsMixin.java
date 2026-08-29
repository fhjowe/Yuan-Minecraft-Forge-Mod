package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raids.class)
public abstract class TimestopRaidsMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopRaidsTick(CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeEntities())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }
}
