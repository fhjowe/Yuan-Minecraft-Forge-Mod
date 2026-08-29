package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class TimestopBlockEntityRenderMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float yuanTimestopBlockEntityPartial(float partialTick) {
        return (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) ? 1.0F : partialTick;
    }
}
