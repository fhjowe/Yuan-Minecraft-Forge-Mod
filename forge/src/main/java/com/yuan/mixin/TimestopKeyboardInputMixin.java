package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class TimestopKeyboardInputMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopKeyboardInput(boolean slowMovement, float sneakFactor, CallbackInfo ci) {
        if (YuanTimeStop.shouldBlockPlayerInput()) {
            ci.cancel();
        }
    }
}
