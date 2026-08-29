package com.yuan.mixin;

import com.yuan.event.YuanSwordEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void onTickNonPassenger(Entity entity, CallbackInfo ci) {
        if (hasTimeWielderPassenger(entity)) return;
        if (!YuanSwordEvents.isEntityFrozen(entity)) return;

        entity.setDeltaMovement(0, 0, 0);
        entity.xo = entity.getX();
        entity.yo = entity.getY();
        entity.zo = entity.getZ();
        entity.xRotO = entity.getXRot();
        entity.yRotO = entity.getYRot();
        ci.cancel();
    }

    private static boolean hasTimeWielderPassenger(Entity entity) {
        for (Entity passenger : entity.getPassengers()) {
            if (YuanSwordEvents.shouldTickVehicleChain(entity.getUUID(), passenger.getUUID())
                    || hasTimeWielderPassenger(passenger)) return true;
        }
        return false;
    }
}
