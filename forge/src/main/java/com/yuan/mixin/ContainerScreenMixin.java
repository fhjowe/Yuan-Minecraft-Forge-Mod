package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class ContainerScreenMixin {

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClicked(Slot slot, int slotId, int mouseButton,
            ClickType clickType, CallbackInfo ci) {
        if (YuanSwordEvents.isClientTimeStopped() && Minecraft.getInstance().player != null
                && !YuanSwordEvents.isClientWielder(Minecraft.getInstance().player.getUUID())) {
            ci.cancel();
        }
    }
}
