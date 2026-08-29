package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class TimestopClientLevelMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopClientLevelTick(CallbackInfo ci) {
        if (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "doAnimateTick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopDoAnimateTick(int x, int y, int z, int data, RandomSource random,
                                           Block block, BlockPos.MutableBlockPos pos, CallbackInfo ci) {
        if (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) {
            ci.cancel();
        }
    }
}
