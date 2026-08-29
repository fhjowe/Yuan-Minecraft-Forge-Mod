package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Util.class)
public class UtilMixin {

    private static long frozenMillis;

    @Inject(method = "getMillis", at = @At("HEAD"), cancellable = true)
    private static void onGetMillis(CallbackInfoReturnable<Long> cir) {
        if (YuanSwordEvents.isClientFullTimeStop()) {
            if (frozenMillis == 0) frozenMillis = System.currentTimeMillis();
            cir.setReturnValue(frozenMillis);
        } else {
            frozenMillis = 0;
        }
    }
}
