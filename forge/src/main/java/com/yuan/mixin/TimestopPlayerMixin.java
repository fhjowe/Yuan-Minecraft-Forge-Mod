package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class TimestopPlayerMixin {
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void timestopHurt(DamageSource source, float amount,
                              CallbackInfoReturnable<Boolean> cir) {
        if (!(source.getEntity() instanceof Player)
                && (YuanTimeStopServerState.isStopped()
                && YuanTimeStopServerState.isInvulnerable()
                || YuanTimeRewindServerState.isRewinding())) {
            cir.setReturnValue(false);
        }
    }
}
