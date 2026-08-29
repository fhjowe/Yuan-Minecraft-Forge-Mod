package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementToast.class)
public abstract class TimestopAdvancementToastMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopAdvancementToast(GuiGraphics guiGraphics, ToastComponent toastComponent, long time,
                                              CallbackInfoReturnable<Toast.Visibility> cir) {
        if (YuanTimeStopServerState.isStopped()) {
            cir.setReturnValue(Toast.Visibility.SHOW);
        }
    }
}
