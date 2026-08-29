package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.LevelTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(LevelTicks.class)
public class LevelTicksMixin<T> {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(long gameTime, int maxTicks, BiConsumer<BlockPos, T> ticker, CallbackInfo ci) {
        if (YuanSwordEvents.shouldFreezeWorldSystems()) ci.cancel();
    }
}
