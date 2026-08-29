package com.yuan.mixin;

import com.yuan.client.YuanKeyBindings;
import com.yuan.timestop.YuanTimeStop;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class TimestopKeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopKeyPress(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (YuanTimeStop.shouldBlockPlayerInput()
                && key != YuanKeyBindings.TRIGGER_TIMESTOP.getKey().getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopKeyboardTick(CallbackInfo ci) {
        if (YuanTimeStop.shouldBlockPlayerInput()) {
            ci.cancel();
        }
    }
}
