package com.yuan.mixin;

import com.yuan.data.YuanBanData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelSpawnMixin {

    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
    private void onAddEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof LivingEntity le) {
            ServerLevel level = (ServerLevel) (Object) this;
            if (YuanBanData.isBanned(level.getServer(), le.getUUID())) {
                cir.setReturnValue(false);
            }
        }
    }
}
