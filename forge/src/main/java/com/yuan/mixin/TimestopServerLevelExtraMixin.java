package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class TimestopServerLevelExtraMixin {
    @Inject(method = "blockUpdated", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopBlockUpdated(BlockPos pos, Block block, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeBlocks())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickChunk", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopTickChunk(net.minecraft.world.level.chunk.LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeBlocks())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickCustomSpawners", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopTickCustomSpawners(boolean spawnEnemies, boolean spawnFriendlies, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeEntities())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }

    @Inject(method = "runBlockEvents", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopRunBlockEvents(CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeBlocks())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }

    @Inject(method = "advanceWeatherCycle", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopAdvanceWeather(CallbackInfo ci) {
        if (YuanTimeStopServerState.isStopped() || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopTickPassenger(Entity vehicle, Entity passenger, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeEntities())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }
}
