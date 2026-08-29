package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class TimestopCreativeScreenMixin {
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopCreativeSlotClicked(Slot slot, int slotId, int button,
                                                 ClickType clickType, CallbackInfo ci) {
        if (YuanTimeStop.shouldBlockPlayerInput()) {
            ci.cancel();
        }
    }
}
