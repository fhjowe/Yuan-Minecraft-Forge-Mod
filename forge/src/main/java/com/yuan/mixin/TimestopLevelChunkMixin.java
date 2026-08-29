package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class TimestopLevelChunkMixin {
    @Inject(method = "isTicking", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopIsTicking(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeBlocks())
                || YuanTimeRewindServerState.isRewinding()) {
            cir.setReturnValue(false);
        }
    }
}
