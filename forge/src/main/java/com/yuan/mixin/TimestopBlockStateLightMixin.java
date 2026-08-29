package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class TimestopBlockStateLightMixin {
    @Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopGetLightEmission(CallbackInfoReturnable<Integer> cir) {
        if (YuanTimeStopServerState.isStopped()) {
            cir.setReturnValue(0);
        }
    }
}
