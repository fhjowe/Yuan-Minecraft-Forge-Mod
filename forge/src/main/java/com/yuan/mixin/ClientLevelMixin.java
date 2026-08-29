package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void onAnimateTick(int x, int y, int z, CallbackInfo ci) {
        if (YuanSwordEvents.isClientFullTimeStop()) ci.cancel();
    }
}
