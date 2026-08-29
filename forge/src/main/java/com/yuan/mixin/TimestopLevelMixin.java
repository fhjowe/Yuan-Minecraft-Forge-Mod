package com.yuan.mixin;

import com.yuan.timestop.YuanTimeStopServerState;
import com.yuan.timerewind.YuanTimeRewindClient;
import com.yuan.timerewind.YuanTimeRewindServerState;
import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class TimestopLevelMixin {
    @Inject(method = "guardEntityTick", at = @At("HEAD"), cancellable = true)
    private void timestopGuardEntityTick(Consumer<Entity> consumer, Entity entity, CallbackInfo ci) {
        // Client-side rewind playback: freeze the behavior (AI/physics) of every non-player entity
        // and of frozen players so the server-sent retreat packets drive the visual without the
        // client entity fighting them (which caused twitching). The renderer interpolation base
        // (xo/yo/zo) is kept aligned each tick by TimestopMinecraftMixin.timestopTick.
        if (YuanTimeRewindClient.isActive()) {
            if (entity instanceof Player) {
                if (!YuanTimeRewindClient.shouldFreezeEntity(entity)) {
                    return;
                }
            }
            ci.cancel();
            return;
        }
        boolean rewinding = YuanTimeRewindServerState.isRewinding();
        if (!YuanTimeStopServerState.isStopped() && !rewinding) {
            return;
        }
        if (rewinding) {
            if (!YuanTimeRewindServerState.shouldFreezeEntity(entity)) {
                return;
            }
            if (entity instanceof LivingEntity living) {
                if (living.hurtTime > 0) {
                    living.hurtTime--;
                }
                living.invulnerableTime = 0;
            }
            ci.cancel();
            return;
        }
        boolean freeze;
        if (entity instanceof Player) {
            freeze = YuanTimeStopServerState.isFreezeSelf();
        } else if (entity instanceof EnderDragon || entity instanceof WitherBoss) {
            freeze = YuanTimeStopServerState.isFreezeBossAI();
        } else {
            freeze = YuanTimeStopServerState.isFreezeEntities();
        }
        if (!freeze) {
            return;
        }
        float radius = YuanTimeStopServerState.getStopRadius();
        if (radius <= 0.0f || entity.distanceToSqr(
                YuanTimeStopServerState.getWielderX(),
                YuanTimeStopServerState.getWielderY(),
                YuanTimeStopServerState.getWielderZ()) <= radius * radius) {
            if (entity instanceof LivingEntity living) {
                if (living.hurtTime > 0) {
                    living.hurtTime--;
                }
                living.invulnerableTime = 0;
            }
            ci.cancel();
        }
    }
}
