package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelTimeMixin {

    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    private void onTickTime(CallbackInfo ci) {
        if (YuanSwordEvents.shouldFreezeWorldSystems()) ci.cancel();
    }

    @Inject(method = "advanceWeatherCycle", at = @At("HEAD"), cancellable = true)
    private void onAdvanceWeather(CallbackInfo ci) {
        if (YuanSwordEvents.shouldFreezeWorldSystems()) ci.cancel();
    }

    @Inject(method = "tickCustomSpawners", at = @At("HEAD"), cancellable = true)
    private void onTickCustomSpawners(boolean spawnEnemies, boolean spawnFriendlies, CallbackInfo ci) {
        if (YuanSwordEvents.shouldFreezeWorldSystems()) ci.cancel();
    }
}
