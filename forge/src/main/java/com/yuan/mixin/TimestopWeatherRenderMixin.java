package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelRenderer.class)
public abstract class TimestopWeatherRenderMixin {

    @ModifyVariable(method = "renderSnowAndRain", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float yuanTimestopSnowAndRain(float partialTick) {
        return (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) ? 1.0F : partialTick;
    }

    @ModifyVariable(method = "renderClouds", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float yuanTimestopClouds(float partialTick) {
        return (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) ? 1.0F : partialTick;
    }

    @ModifyVariable(method = "renderSky", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float yuanTimestopSky(float partialTick) {
        return (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) ? 1.0F : partialTick;
    }
}
