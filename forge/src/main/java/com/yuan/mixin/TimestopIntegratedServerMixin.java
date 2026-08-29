package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStop;
import com.yuan.timerewind.YuanTimeRewindClient;
import java.util.function.BooleanSupplier;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServer.class)
public class TimestopIntegratedServerMixin {
    @Inject(method = "tickServer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/server/IntegratedServer;getProfiler()Lnet/minecraft/util/profiling/ProfilerFiller;"))
    private void timestopTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (YuanTimeStop.get() || YuanTimeRewindClient.isActive()) {
            IntegratedServer server = (IntegratedServer) (Object) this;
            server.paused = false;
        }
    }
}
