package com.yuan.mixin;

import com.yuan.timerewind.YuanTimeRewindEvents;
import com.yuan.timerewind.YuanTimeRewindRecorder;
import com.yuan.timerewind.YuanTimeRewindServerState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class TimerewindServerLevelMixin {
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            at = @At("HEAD"))
    private void yuan_recordBlockChange(BlockPos pos, BlockState state, int flags,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (YuanTimeRewindServerState.isRewinding()) return;
        // ServerLevel inherits the 3-arg setBlock from Level in 1.20.1.
        if (!((Object) this instanceof ServerLevel)) return;
        ServerLevel level = (ServerLevel) (Object) this;
        YuanTimeRewindRecorder recorder = YuanTimeRewindEvents.recorder(level);
        if (recorder == null) return;
        BlockState old = level.getBlockState(pos);
        if (old == state || old.equals(state)) return;
        BlockEntity oldBe = level.getBlockEntity(pos);
        CompoundTag oldNbt = oldBe == null ? null : oldBe.saveWithFullMetadata();
        recorder.recordBlockChange(level.getGameTime(), pos, old, oldNbt);
    }
}
