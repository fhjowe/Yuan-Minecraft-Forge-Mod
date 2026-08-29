package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SoundManager.class)
public abstract class TimestopSoundManagerMixin {
    @ModifyVariable(method = "tick", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private boolean yuanTimestopSoundPaused(boolean paused) {
        if (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) {
            return true;
        }
        return paused;
    }
}
