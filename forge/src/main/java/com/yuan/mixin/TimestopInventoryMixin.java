package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class TimestopInventoryMixin {
    @Inject(method = "swapPaint", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopSwapPaint(double direction, CallbackInfo ci) {
        if (YuanTimeStop.shouldBlockPlayerInput()) {
            ci.cancel();
        }
    }
}
