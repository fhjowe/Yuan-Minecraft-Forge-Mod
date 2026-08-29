package com.yuan.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.yuan.timestop.YuanTimeStop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderStateShard.class)
public class TimestopRenderStateShardMixin {
    @WrapOperation(method = "setupGlintTexturing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/Util;getMillis()J", ordinal = 0))
    private static long timestopGlint(Operation<Long> original) {
        if (!YuanTimeStop.get()) {
            return original.call();
        }
        return (long) ((double) YuanTimeStop.millis
                * (Double) Minecraft.getInstance().options.glintSpeed().get() * 8.0);
    }
}
