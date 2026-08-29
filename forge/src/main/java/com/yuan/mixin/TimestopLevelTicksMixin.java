package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.LevelTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelTicks.class)
public abstract class TimestopLevelTicksMixin<T> {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopLevelTicks(long gameTime, int maxTicks, BiConsumer<BlockPos, T> ticker, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeBlocks())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }
}
