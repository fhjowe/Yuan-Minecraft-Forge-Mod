package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class TimestopParticleSpawnMixin {
    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopParticleSpawn(Particle particle, CallbackInfo ci) {
        if (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) {
            ci.cancel();
        }
    }
}
