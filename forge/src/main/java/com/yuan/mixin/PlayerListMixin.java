package com.yuan.mixin;

import com.yuan.event.YuanDefenseState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Redirect(method = "remove", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;removePlayerImmediately(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity$RemovalReason;)V"))
    private void aroundLogoutRemoval(ServerLevel level, ServerPlayer player, Entity.RemovalReason reason) {
        lifecycleRemoval(level, player, reason);
    }

    @Redirect(method = "respawn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;removePlayerImmediately(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity$RemovalReason;)V"))
    private void aroundRespawnRemoval(ServerLevel level, ServerPlayer player, Entity.RemovalReason reason) {
        lifecycleRemoval(level, player, reason);
    }

    private static void lifecycleRemoval(ServerLevel level, ServerPlayer player, Entity.RemovalReason reason) {
        try (YuanDefenseState.Scope ignored = YuanDefenseState.enterLifecycleRemoval()) {
            level.removePlayerImmediately(player, reason);
        }
    }
}
