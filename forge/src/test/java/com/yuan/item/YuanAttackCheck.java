package com.yuan.item;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.server.Bootstrap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class YuanAttackCheck {
    public static void main(String[] args) throws Exception {
        allowRegistryInitialization();
        assert YuanSwordItem.AttackMode.values().length == 5;
        assert YuanSwordItem.AttackMode.fromId(Integer.MIN_VALUE) == YuanSwordItem.AttackMode.RUIN;
        assert YuanSwordItem.AttackMode.fromId(-1) == YuanSwordItem.AttackMode.ABSOLUTE;
        assert YuanSwordItem.AttackMode.OBLIVION.next() == YuanSwordItem.AttackMode.ABSOLUTE;
        assert YuanSwordItem.AttackMode.ABSOLUTE.next() == YuanSwordItem.AttackMode.ANNIHILATE;
        assert YuanSwordItem.AttackMode.ANNIHILATE.prev() == YuanSwordItem.AttackMode.ABSOLUTE;
        assert !YuanSwordItem.targetPolicy(false, true, false, false, false, false,
                false, false, false, false, true) : "players are excluded by default";
        assert YuanSwordItem.targetPolicy(false, true, false, false, false, false,
                true, false, false, false, true) : "configured players are included";
        assert !YuanSwordItem.targetPolicy(true, false, false, false, false, false,
                true, true, true, true, true) : "self is always excluded";
        assert !YuanSwordItem.targetPolicy(false, false, true, false, false, false,
                true, false, true, true, true) : "allies follow config";
        assert !YuanSwordItem.targetPolicy(false, false, false, true, false, false,
                true, true, false, true, true) : "tamed mobs follow config";
        assert !YuanSwordItem.targetPolicy(false, false, false, false, true, false,
                true, true, true, false, true) : "villagers follow config";
        assert !YuanSwordItem.targetPolicy(false, false, false, false, false, true,
                true, true, true, true, false) : "bosses follow config";
        assert YuanSwordItem.targetPolicy(false, false, false, false, false, true,
                true, true, true, true, true);
        assert !YuanKillHelper.shouldRemoveAfterNormalKill(false, false);
        assert YuanKillHelper.shouldRemoveAfterNormalKill(true, false);
        assert !YuanKillHelper.shouldRemoveAfterNormalKill(true, true) : "dragon lifecycle must not be shortcut";
        assert YuanKillHelper.shouldUseExplicitDeathFallback(true, false);
        assert !YuanKillHelper.shouldUseExplicitDeathFallback(false, false);
        assert !YuanKillHelper.shouldUseExplicitDeathFallback(true, true)
                : "dragon must remain on its damage lifecycle";
        assert !YuanKillHelper.shouldApplyReentry(0);
        assert YuanKillHelper.shouldApplyReentry(1);
        assert YuanKillHelper.shouldApplyReentry(2) : "reentry policy is entity-type agnostic";
        assert !YuanKillHelper.shouldSuppressDrops(false, false) : "immediate removal cannot leak suppression UUIDs";
        assert YuanKillHelper.shouldSuppressDrops(false, true);
        assert !YuanKillHelper.shouldSuppressDrops(true, true);
        UUID suppressed = UUID.randomUUID();
        long markedAt = 1_000L;
        YuanKillHelper.markNoDrops(suppressed, markedAt);
        assert YuanKillHelper.consumeNoDrops(suppressed, markedAt + YuanKillHelper.NO_DROPS_TTL_NANOS - 1);
        assert !YuanKillHelper.consumeNoDrops(suppressed, markedAt + YuanKillHelper.NO_DROPS_TTL_NANOS - 1)
                : "drop marker is consumed once";
        YuanKillHelper.markNoDrops(suppressed, markedAt);
        assert !YuanKillHelper.consumeNoDrops(suppressed, markedAt + YuanKillHelper.NO_DROPS_TTL_NANOS)
                : "stale drop marker expires";
        YuanKillHelper.markNoDrops(suppressed, markedAt);
        YuanKillHelper.cancelNoDrops(suppressed);
        assert !YuanKillHelper.consumeNoDrops(suppressed, markedAt)
                : "immediate public removal cancels suppression";
        Object directParent = new Object();
        Object otherParent = new Object();
        Set<Object> seen = new HashSet<>();
        assert !YuanSwordItem.addDistinctTarget(seen, directParent, directParent);
        assert YuanSwordItem.addDistinctTarget(seen, directParent, otherParent);
        assert !YuanSwordItem.addDistinctTarget(seen, directParent, otherParent) : "multipart parents are deduplicated";
        net.minecraft.world.entity.Entity target = null;
        assert !YuanAbsoluteAttack.isActive(target);
        try (YuanAbsoluteAttack.Scope ignored = YuanAbsoluteAttack.enter(target)) {
            assert YuanAbsoluteAttack.isActive(target);
            try (YuanAbsoluteAttack.Scope nested = YuanAbsoluteAttack.enter(target)) {
                assert YuanAbsoluteAttack.isActive(target) : "nested scope remains active";
            }
            assert YuanAbsoluteAttack.isActive(target) : "outer scope survives nested cleanup";
        }
        assert !YuanAbsoluteAttack.isActive(target) : "normal cleanup exits context";
        try {
            try (YuanAbsoluteAttack.Scope ignored = YuanAbsoluteAttack.enter(target)) {
                throw new IllegalStateException("expected");
            }
        } catch (IllegalStateException expected) { }
        assert !YuanAbsoluteAttack.isActive(target) : "exception cleanup exits context";
        assert YuanSwordItem.chargeTicks(71970) == 30;
        assert !YuanSwordItem.shouldPulse(0) : "use() owns the press-time AOE";
        assert !YuanSwordItem.shouldPulse(1);
        assert YuanSwordItem.shouldPulse(5);
        assert !YuanSwordItem.isWorldCharge(79);
        assert YuanSwordItem.isWorldCharge(80);
        assert YuanSwordItem.isInRange(0, 0, 0, 3, 4, 0, 5);
        assert !YuanSwordItem.isInRange(0, 0, 0, 3, 4, 1, 5);
        assert YuanSwordItem.isInRange(0, 0, 0, 400, 0, 0, 400)
                : "purge spherical boundary is inclusive";
        assert !YuanSwordItem.isInRange(0, 0, 0, 400.01, 0, 0, 400)
                : "purge excludes targets past its spherical boundary";
        assert YuanSwordItem.isInCorridor(0, 0, 0, 0, 0, 1, 0, 0, 27, 27, 3)
                : "target ahead inside corridor";
        assert !YuanSwordItem.isInCorridor(0, 0, 0, 0, 0, 1, 3.01, 0, 10, 27, 3)
                : "target outside fixed lateral radius";
        assert !YuanSwordItem.isInCorridor(0, 0, 0, 0, 0, 1, 0, 0, -1, 27, 3)
                : "target behind source";
        assert YuanSwordItem.isInCorridor(0, 0, 0, 0, 0, 1, 0, 0, 27, 27, 3)
                : "range boundary is inclusive";
        assert !YuanSwordItem.isInCorridor(0, 0, 0, 0, 0, 1, 0, 0, 27.01, 27, 3)
                : "past range boundary is excluded";
        assert YuanSwordItem.compareTargetDistance(4, 9) < 0 : "corridor sorts nearest first";
        assert YuanSwordItem.compareTargetDistance(9, 4) > 0;
        assert YuanSwordItem.compareTargetDistance(4, 4) == 0;
        assert YuanSwordItem.hasTargetCapacity(1, 1, 3);
        assert !YuanSwordItem.hasTargetCapacity(2, 1, 3) : "combined attacks share the configured limit";
        assert YuanSwordItem.isBlockPathAccepted(false, true) : "disabled clip ignores obstruction";
        assert YuanSwordItem.isBlockPathAccepted(true, false) : "enabled clip accepts clear paths";
        assert !YuanSwordItem.isBlockPathAccepted(true, true) : "enabled clip rejects obstruction";
        Set<Object> eligible = new HashSet<>();
        Object normalizedParent = new Object();
        assert YuanSwordItem.addEligibleTarget(eligible, normalizedParent, false, true, true, 0, 2);
        assert !YuanSwordItem.addEligibleTarget(eligible, normalizedParent, false, true, true, 0, 2)
                : "normalized multipart parents are deduplicated";
        assert !YuanSwordItem.addEligibleTarget(eligible, new Object(), false, false, true, 0, 2)
                : "shared target policy is required";
        assert !YuanSwordItem.addEligibleTarget(eligible, new Object(), false, true, false, 0, 2)
                : "spatial filtering is required";
        assert !YuanSwordItem.addEligibleTarget(eligible, new Object(), true, true, true, 0, 2)
                : "already attacked targets remain deduplicated";
        assert !YuanSwordItem.addEligibleTarget(eligible, new Object(), false, true, true, 1, 2)
                : "candidate and prior attacks share the target cap";
        assert !YuanSwordItem.shouldReleaseImmediate(0) && !YuanSwordItem.shouldReleaseCharged(0);
        assert YuanSwordItem.shouldReleaseImmediate(1) && !YuanSwordItem.shouldReleaseCharged(1);
        assert !YuanSwordItem.shouldReleaseImmediate(2) && YuanSwordItem.shouldReleaseCharged(2);
        assert YuanSwordItem.shouldReleaseImmediate(3) && YuanSwordItem.shouldReleaseCharged(3);
        assert YuanSwordItem.shouldPurgeOnRelease(true, 1) : "crouch immediate release purges";
        assert !YuanSwordItem.shouldPurgeOnRelease(false, 1) : "standing immediate release uses corridor";
        assert !YuanSwordItem.shouldPurgeOnRelease(true, 0) : "disabled release cannot purge";
        assert !YuanSwordItem.shouldPurgeOnRelease(true, 2) : "charged-only mode does not trigger immediate purge";
        assert YuanSwordItem.shouldPurgeOnRelease(true, 3) : "combined mode permits crouch purge";
        assert YuanSwordItem.shouldRenderRuinExplosion(true);
        assert !YuanSwordItem.shouldRenderRuinExplosion(false);
        assert YuanSwordItem.attackSpeedModifier(100) == 100.0;
        assert YuanSwordItem.rightLightningCount(0, 7, 12) == 7;
        assert YuanSwordItem.rightLightningCount(1, 7, 12) == 12;
        assert YuanSwordItem.rightLightningCount(1, 7, 0) == 0;
        assert YuanKillHelper.lightningOffset(0, .75) == 0;
        assert YuanKillHelper.lightningOffset(128, 1) == 128;
        assert YuanKillHelper.lightningOffset(128, -1) == -128;

        assert YuanSwordItem.attackDamageModifier(0) == null : "mode 0 preserves the original modifier";
        AttributeModifier finiteDamage = YuanSwordItem.attackDamageModifier(1);
        assert finiteDamage != null && finiteDamage.getAmount() == 1.0E30D
                : "finite extreme damage must use the exact safe finite value";
        assert Float.isFinite((float) finiteDamage.getAmount())
                : "finite extreme damage must remain finite after float conversion";
        AttributeModifier infiniteDamage = YuanSwordItem.attackDamageModifier(2);
        assert infiniteDamage != null && infiniteDamage.getAmount() == Double.POSITIVE_INFINITY;
        assert !Float.isFinite((float) infiniteDamage.getAmount());
        assert YuanKillHelper.shouldScheduleDragonFallback(YuanSwordItem.AttackMode.ABSOLUTE, true);
        assert !YuanKillHelper.shouldScheduleDragonFallback(YuanSwordItem.AttackMode.ANNIHILATE, true)
                : "normal annihilate preserves the dragon lifecycle even when damage is blocked";
        assert !YuanKillHelper.shouldScheduleDragonFallback(YuanSwordItem.AttackMode.ABSOLUTE, false);
        assert YuanKillHelper.shouldRemovePendingDragon(true, false, false, 0);
        assert !YuanKillHelper.shouldRemovePendingDragon(true, true, false, 0)
                : "death progression cancels absolute fallback removal";

        String source = Files.readString(Path.of("src/main/java/com/yuan/item/YuanSwordItem.java"));
        String helper = Files.readString(Path.of("src/main/java/com/yuan/item/YuanKillHelper.java"));
        String events = Files.readString(Path.of("src/main/java/com/yuan/event/YuanSwordEvents.java"));
        String normalKill = helper.substring(helper.indexOf("public static void kill("),
                helper.indexOf("static boolean shouldRemoveAfterNormalKill"));
        assert normalKill.indexOf("playerAttack(player)") < normalKill.indexOf("fellOutOfWorld()")
                : "normal annihilate must attempt attributed player damage first";
        assert !helper.contains("configStack(") : "effect helpers must use the attacking stack";
        assert helper.contains("kill(Entity entity, Player player, ItemStack cfg)");
        assert helper.contains("halfHealth(Entity entity, Player player, ItemStack cfg)");
        assert helper.contains("obliterate(Entity entity, Player player, ItemStack cfg)");
        assert helper.contains("erase(Entity entity, Player player, ItemStack cfg)");
        assert helper.contains("absoluteErase(Entity entity, Player player, ItemStack cfg)");
        assert !helper.contains("if (le.isAlive()) le.die") : "die fallback must not be gated after setHealth";
        assert helper.contains("le.setHealth(0);\n            le.die(")
                : "fallback must set zero health then explicitly enter attributed death";
        assert !helper.contains("finally") : "drop suppression must survive delayed death";
        assert events.contains("YuanSwordItem.isTarget(attacker, player, cfg)")
                : "counterattack must share target policy";
        assert events.contains("YuanKillHelper.kill(attacker, player, cfg)")
                : "counterattack must use its selected defense stack";
        assert events.contains("YuanWeaponBinding.canUseWeapon(serverPlayer, cfg)")
                : "counterattack must require the authoritative bound weapon";
        String leftClick = source.substring(source.indexOf("public boolean onLeftClickEntity"),
                source.indexOf("// ==================== Ray", source.indexOf("public boolean onLeftClickEntity")));
        assert leftClick.contains("canUseWeapon") : "left-click entry must require weapon authority";
        String use = source.substring(source.indexOf("public InteractionResultHolder<ItemStack> use"),
                source.indexOf("// ==================== Right‑click hold"));
        assert use.contains("canUseWeapon") : "use press must require weapon authority";
        String useTick = source.substring(source.indexOf("public void onUseTick"),
                source.indexOf("// ==================== Right‑click release"));
        assert useTick.contains("canUseWeapon") : "held pulse must require weapon authority";
        String release = source.substring(source.indexOf("public void releaseUsing"),
                source.indexOf("static int chargeTicks"));
        assert release.contains("canUseWeapon") : "release, corridor, purge and charged attacks require authority";
        assert !source.contains("MAX_AOE_TARGETS = 128") : "configured attack paths must not use a hard-coded cap";
        assert source.contains("maxAttackTargets(stack)") : "all configured AOE paths must share one cap helper";
        assert helper.contains("queueDragonFallback") : "absolute dragon attack must schedule a grace fallback";
        assert helper.contains("tickPendingDragonFallbacks") : "dragon fallback must be rechecked on server tick";
        assert helper.contains("deathTime") : "fallback must preserve progressing vanilla dragon death";
        assert !normalKill.contains("queueDragonFallback")
                : "normal annihilate must never schedule dragon public removal";
        assert helper.contains("cleanupNoDrops") : "no-drop markers need periodic cleanup without consume lookup";
        assert !leftClick.contains("spawnHitEffects") : "left-click attacks must not spawn gilded hit particles";
        assert source.substring(source.indexOf("private int attackLoadedEntities"),
                source.indexOf("private boolean applyModeToEntity")).contains("spawnHitEffects")
                : "AOE hit effects must remain";
        assert source.contains("K_RIGHT_LIGHTNING_ENABLED");
        assert source.contains("renderRightClickLightning");
        String corridor = source.substring(source.indexOf("private void attackCorridor"),
                source.indexOf("static boolean shouldRenderRuinExplosion"));
        assert corridor.contains("renderRightClickLightning") : "corridor release must render configured lightning";
        assert source.substring(source.indexOf("private int attackLoadedEntities"),
                source.indexOf("private boolean applyModeToEntity")).contains("renderRightClickLightning")
                : "AOE pulses must render configured right-click lightning";
        assert helper.contains("bolt.setVisualOnly(true)");
        assert helper.contains("bolt.setSilent(!sound)");
    }

    private static void allowRegistryInitialization() throws Exception {
        try {
            Bootstrap.bootStrap();
        } catch (ExceptionInInitializerError forgeNetworkBootstrapFailure) {
            // Forge 47.4.20 initializes registries before its standalone NetworkEvent bootstrap fails.
        }
    }
}
