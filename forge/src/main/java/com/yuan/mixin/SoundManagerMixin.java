package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(boolean isActive, CallbackInfo ci) {
        if (YuanSwordEvents.isClientFullTimeStop()) {
            ci.cancel();
        }
    }
}
