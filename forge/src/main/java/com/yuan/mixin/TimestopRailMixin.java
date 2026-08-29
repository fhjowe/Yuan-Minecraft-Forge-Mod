package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RailBlock.class)
public abstract class TimestopRailMixin {
    @Inject(method = "updateState", at = @At("HEAD"), cancellable = true)
    private void timestopUpdateState(BlockState state, Level level, BlockPos pos,
                                     Block neighbor, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeBlocks())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }
}
