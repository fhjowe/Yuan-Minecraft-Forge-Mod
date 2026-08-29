package com.yuan;

import java.nio.file.Files;
import java.nio.file.Path;

public final class MixinConfigCheck {
    public static void main(String[] args) throws Exception {
        String config = Files.readString(Path.of("src/main/resources/yuan.mixins.json"));
        assert config.contains("ItemRendererMixin") : "cosmic item renderer mixin must be registered";
        assert !config.contains("UtilMixin");

        String timeMixin = Files.readString(Path.of(
                "src/main/java/com/yuan/mixin/ServerLevelTimeMixin.java"));
        assert timeMixin.contains("method = \"tickTime\"");

        String entityMixin = Files.readString(Path.of(
                "src/main/java/com/yuan/mixin/EntityMixin.java"));
        assert entityMixin.contains("method = \"remove\"");
        assert entityMixin.contains("method = \"setRemoved\"");
        assert entityMixin.contains("YuanSwordEvents.shouldProtectRemoval");

        String livingMixin = Files.readString(Path.of(
                "src/main/java/com/yuan/mixin/LivingEntityMixin.java"));
        assert livingMixin.contains("method = \"hurt\"");
        assert livingMixin.contains("method = \"setHealth\"");
        assert livingMixin.contains("YuanSwordEvents.shouldBlockDamage");
        assert livingMixin.contains("YuanSwordEvents.shouldBlockHealthSet");
        assert livingMixin.contains("@ModifyArg(method = \"actuallyHurt\"")
                : "health authorization must wrap the setHealth call in actuallyHurt";
        assert !livingMixin.contains("@ModifyArg(method = \"hurt\"");
        assert livingMixin.contains("target = \"Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V\"");
        assert livingMixin.contains("require = 1");
        assert livingMixin.contains("expect = 1")
                : "startup must fail loudly if the composable hurt hook no longer matches exactly once";
        assert livingMixin.contains("YuanDefenseState.authorizeHealthDecrease(player)");
        assert livingMixin.contains("YuanDefenseState.consumeHealthDecrease(player)");
        assert !livingMixin.contains("@Redirect");
        assert !livingMixin.contains("@Inject(method = \"hurt\", at = @At(\"RETURN\"))");
        assert !livingMixin.contains("YUAN_HEALTH_DECREASE");
        assert livingMixin.contains("level().isClientSide");

        String swordEvents = Files.readString(Path.of(
                "src/main/java/com/yuan/event/YuanSwordEvents.java"));
        assert swordEvents.contains("if (player.getInventory() == null) return ItemStack.EMPTY;")
                : "defense lookup must tolerate Player construction before inventory initialization";

        String commandsMixin = Files.readString(Path.of(
                "src/main/java/com/yuan/mixin/CommandsMixin.java"));
        assert commandsMixin.contains("method = \"performCommand\"");
        assert commandsMixin.contains("CommandDispatcher;execute")
                : "direct function/command-block execution must enter the administrative scope";
        assert commandsMixin.contains("enterAdministrativeCommand");
        assert commandsMixin.contains("hasPermission(2)");
        assert commandsMixin.contains("try (");

        String playerListMixin = Files.readString(Path.of(
                "src/main/java/com/yuan/mixin/PlayerListMixin.java"));
        assert playerListMixin.contains("target = \"Lnet/minecraft/server/level/ServerLevel;removePlayerImmediately(")
                : "logout and respawn must wrap PlayerList's actual removal call";
        assert !playerListMixin.contains("target = \"Lnet/minecraft/server/level/ServerPlayer;remove(");
        assert playerListMixin.contains("method = \"respawn\"");
        assert playerListMixin.contains("enterLifecycleRemoval");
        assert playerListMixin.contains("try (");
    }
}
