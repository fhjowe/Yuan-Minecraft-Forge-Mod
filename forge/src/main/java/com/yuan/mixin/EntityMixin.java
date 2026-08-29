package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        if ((Object) this instanceof Player player && YuanSwordEvents.shouldProtectRemoval(player, reason)) ci.cancel();
    }

    @Inject(method = "setRemoved", at = @At("HEAD"), cancellable = true)
    private void onSetRemoved(Entity.RemovalReason reason, CallbackInfo ci) {
        if ((Object) this instanceof Player player && YuanSwordEvents.shouldProtectRemoval(player, reason)) ci.cancel();
    }
}
