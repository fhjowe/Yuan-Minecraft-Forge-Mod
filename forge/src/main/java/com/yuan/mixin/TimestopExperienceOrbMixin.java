package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindServerState;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class TimestopExperienceOrbMixin {
    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void yuanTimestopExpOrbPlayerTouch(Player player, CallbackInfo ci) {
        if ((YuanTimeStopServerState.isStopped() && YuanTimeStopServerState.isFreezeEntities())
                || YuanTimeRewindServerState.isRewinding()) {
            ci.cancel();
        }
    }
}
