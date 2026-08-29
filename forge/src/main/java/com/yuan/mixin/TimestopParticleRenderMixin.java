package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class TimestopParticleRenderMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopParticleTick(CallbackInfo ci) {
        if (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "renderParticles", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float yuanTimestopParticlePartial(float partialTick) {
        return (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) ? 1.0F : partialTick;
    }
}
