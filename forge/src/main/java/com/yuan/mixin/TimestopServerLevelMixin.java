package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class TimestopServerLevelMixin extends Level {
    protected TimestopServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension,
                                       RegistryAccess registryAccess,
                                       Holder<DimensionType> dimensionType,
                                       Supplier<ProfilerFiller> profiler,
                                       boolean clientSide, boolean debug,
                                       long biomeZoomSeed, int maxChunkNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionType,
                profiler, clientSide, debug, biomeZoomSeed, maxChunkNeighborUpdates);
    }

    @Inject(method = "tickTime", at = @At("HEAD"), cancellable = true)
    private void timestopTickTime(CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeBlocks())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }

    @Inject(method = "tickBlock", at = @At("HEAD"), cancellable = true)
    private void timestopTickBlock(BlockPos pos, Block block, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeBlocks())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }
}
