package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class TimestopItemInHandMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopItemInHandTick(CallbackInfo ci) {
        if (YuanTimeStop.shouldCancelItemInHandTick()) {
            ci.cancel();
        }
    }
}
