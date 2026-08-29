package com.yuan.mixin;

import com.yuan.client.YuanKeyBindings;
import com.yuan.timestop.YuanTimeStop;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public abstract class TimestopKeyMappingMixin {
    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopConsumeClick(CallbackInfoReturnable<Boolean> cir) {
        if (YuanTimeStop.shouldBlockPlayerInput()
                && (Object) this != YuanKeyBindings.TRIGGER_TIMESTOP) {
            cir.setReturnValue(false);
        }
    }
}
