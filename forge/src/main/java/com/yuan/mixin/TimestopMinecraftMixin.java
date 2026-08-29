package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timestop.YuanTimeStopRenderEvent;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class TimestopMinecraftMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void timestopTick(CallbackInfo ci) {
        if (!YuanTimeStop.get() && !YuanTimeRewindClient.isActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.entitiesForRendering().forEach(entity -> {
                if (!entity.isRemoved()) {
                    YuanTimeStop.freezeLerp(entity);
                    if (YuanTimeRewindClient.isActive()
                            && !YuanTimeRewindClient.shouldFreezeEntity(entity)) {
                        // During animated playback the non-frozen entities (the server-driven
                        // retreat ones) have their client tick cancelled in TimestopLevelMixin, so
                        // setOldPosAndRot never runs. Advance the old-position fields here so the
                        // renderer interpolates between consecutive retreat packets smoothly
                        // instead of lerping from a stale base (which looked like twitching).
                        entity.xo = entity.getX();
                        entity.yo = entity.getY();
                        entity.zo = entity.getZ();
                        entity.xOld = entity.getX();
                        entity.yOld = entity.getY();
                        entity.zOld = entity.getZ();
                        entity.yRotO = entity.getYRot();
                        entity.xRotO = entity.getXRot();
                    }
                }
            });
        }
    }

    @Inject(method = "run",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/metrics/profiling/MetricsRecorder;endTick()V"))
    private void timestopRun(CallbackInfo ci) {
        if (Minecraft.getInstance().level == null && YuanTimeStop.realMillis > 3000L) {
            YuanTimeStop.setIsTimeStop(false);
        }
    }

    @Inject(method = "runTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"))
    private void timestopPostEvent(boolean renderLevel, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(
                new YuanTimeStopRenderEvent(TickEvent.Phase.START, Minecraft.getInstance().realPartialTick));
    }
}
