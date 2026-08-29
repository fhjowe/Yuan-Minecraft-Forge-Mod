package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelTickMixin {

    @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true)
    private void onTickPassenger(Entity vehicle, Entity passenger, CallbackInfo ci) {
        if (YuanSwordEvents.isEntityFrozen(passenger)) ci.cancel();
    }
}
