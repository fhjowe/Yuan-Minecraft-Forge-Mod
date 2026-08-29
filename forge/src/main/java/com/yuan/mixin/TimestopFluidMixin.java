package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidState.class)
public abstract class TimestopFluidMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void timestopScheduledTick(Level level, BlockPos pos, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeFluids())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void timestopRandomTick(Level level, BlockPos pos,
                                    RandomSource random, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeFluids())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }

    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void timestopAnimateTick(Level level, BlockPos pos,
                                     RandomSource random, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeFluids())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }
}
