package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class TimestopCameraMixin {
    @ModifyVariable(method = "setup", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float timestopPartial(float value, BlockGetter level, Entity entity) {
        if (YuanTimeStop.get() && entity != null && !YuanTimeStop.shouldFreezeEntity(entity)) {
            return YuanTimeStop.livePartialTick();
        }
        return value;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void timestopTick(CallbackInfo ci) {
        Camera camera = (Camera) (Object) this;
        if (YuanTimeStop.shouldCancelCameraTick(camera) || YuanTimeRewindClient.shouldFreezeCamera()) {
            ci.cancel();
        }
    }
}
