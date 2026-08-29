package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import com.yuan.event.YuanDefenseState;
import com.yuan.item.YuanConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "aiStep", at = @At("HEAD"), cancellable = true)
    private void onAiStep(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        boolean frozen = self.level().isClientSide
                ? YuanSwordEvents.isClientEntityFrozen(self.getUUID(),
                    self.level().dimension().location().toString(), self.getX(), self.getY(), self.getZ())
                : YuanSwordEvents.isEntityFrozen(self);
        if (frozen) ci.cancel();
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide && self instanceof Player player
                && YuanSwordEvents.shouldBlockDamage(player, source, YuanConfig.K_DEFENSE_HURT)) {
            cir.setReturnValue(false);
        }
    }

    @ModifyArg(method = "actuallyHurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"), require = 1, expect = 1)
    private float authorizeHurtHealthDecrease(float health) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player && health < player.getHealth())
            YuanDefenseState.authorizeHealthDecrease(player);
        return health;
    }

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void onSetHealth(float health, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.level().isClientSide && self instanceof Player player) {
            boolean authorized = YuanDefenseState.consumeHealthDecrease(player);
            if (YuanSwordEvents.shouldBlockHealthSet(player, health, authorized)) ci.cancel();
        }
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void onPush(Entity other, CallbackInfo ci) {
        if ((Object) this instanceof Player player && YuanSwordEvents.protects(player, YuanConfig.K_DEFENSE_PUSH)) {
            ci.cancel();
        }
    }

    @Inject(method = "isPushable", at = @At("RETURN"), cancellable = true)
    private void onIsPushable(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player && YuanSwordEvents.protects(player, YuanConfig.K_DEFENSE_PUSH)) {
            cir.setReturnValue(false);
        }
    }
}
