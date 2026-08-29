package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderDispatcher.class)
public abstract class TimestopEntityRenderMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private float yuanTimestopPartial(float partialTick, Entity entity) {
        if (YuanTimeRewindClient.isActive()) {
            return YuanTimeRewindClient.shouldFreezeEntity(entity) ? 1.0F : partialTick;
        }
        if (!YuanTimeStop.get()) {
            return partialTick;
        }
        return YuanTimeStop.shouldFreezeEntity(entity) ? 1.0F : YuanTimeStop.livePartialTick();
    }
}
