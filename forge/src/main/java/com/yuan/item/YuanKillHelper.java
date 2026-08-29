package com.yuan.item;

import com.yuan.data.YuanBanData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.PartEntity;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;

public class YuanKillHelper {

    static final long NO_DROPS_TTL_NANOS = 5L * 60 * 1_000_000_000;
    private static final Map<UUID, Long> NO_DROPS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> PENDING_DRAGONS = new ConcurrentHashMap<>();
    private static final int DRAGON_GRACE_TICKS = 20;
    private static final int MAX_PENDING_DRAGONS = 256;

    public static void kill(Entity entity, Player player, ItemStack cfg) {
        if (entity == player || !(entity instanceof LivingEntity le)) return;
        int strength = YuanConfig.getInt(cfg, YuanConfig.K_KILL_STRENGTH, 0);
        switch (strength) {
            case 1: halfHealth(entity, player, cfg); return;
            case 2: doKillPercent(le, player, cfg, 0.1f); return;
            case 3: obliterate(entity, player, cfg); return;
            default: break; // full kill
        }

        le.hurtMarked = false;
        le.invulnerableTime = 0;
        if (YuanConfig.get(cfg, YuanConfig.K_STRIP, true)) le.setAbsorptionAmount(0.0F);

        le.hurt(player.damageSources().playerAttack(player), Float.MAX_VALUE);
        if (le.isAlive()) le.hurt(le.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
        if (YuanConfig.get(cfg, YuanConfig.K_HIT11, true) && le.isAlive()) {
            for (int i = 0; i < 10; i++) le.hurt(le.damageSources().magic(), Float.MAX_VALUE);
        }

        if (shouldUseExplicitDeathFallback(le.isAlive(), le instanceof EnderDragon)) {
            le.setHealth(0);
            le.die(player.damageSources().playerAttack(player));
        }
        if (shouldRemoveAfterNormalKill(le.isAlive(), le instanceof EnderDragon)) {
            le.remove(Entity.RemovalReason.KILLED);
        }
    }

    static boolean shouldRemoveAfterNormalKill(boolean alive, boolean enderDragon) {
        return alive && !enderDragon;
    }

    static boolean shouldUseExplicitDeathFallback(boolean alive, boolean enderDragon) {
        return alive && !enderDragon;
    }

    private static void doKillPercent(LivingEntity le, Player player, ItemStack cfg, float pct) {
        le.invulnerableTime = 0;
        if (YuanConfig.get(cfg, YuanConfig.K_STRIP, true)) le.setAbsorptionAmount(0.0F);
        le.setHealth(Math.max(le.getMaxHealth() * pct, 0.1f));
        le.hurtMarked = true;
    }

    public static void halfHealth(Entity entity, Player player, ItemStack cfg) {
        if (entity == player || !(entity instanceof LivingEntity le)) return;
        le.invulnerableTime = 0;
        float pct = YuanConfig.getFloat(cfg, YuanConfig.K_TORMENT_PCT, 50f) / 100f;
        if (YuanConfig.get(cfg, YuanConfig.K_STRIP, true)) le.setAbsorptionAmount(0.0F);
        le.setHealth(Math.max(le.getHealth() * (1f - pct), 0.1f));
        le.hurtMarked = true;
    }

    public static void obliterate(Entity entity, Player player, ItemStack cfg) {
        if (entity == player || !(entity instanceof LivingEntity le)) return;
        le.hurtMarked = false;
        le.invulnerableTime = 0;
        if (YuanConfig.get(cfg, YuanConfig.K_STRIP, true)) le.setAbsorptionAmount(0.0F);
        float power = YuanConfig.getFloat(cfg, YuanConfig.K_RUIN_POWER, 10f);
        double dx = entity.getX() - player.getX();
        double dz = entity.getZ() - player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0.01) { dx /= dist; dz /= dist; }
        le.knockback(power, dx, dz);
        le.hurtMarked = true;
    }

    public static void erase(Entity entity, Player player, ItemStack cfg) {
        if (!(entity instanceof LivingEntity le)) return;
        boolean ban = YuanConfig.get(cfg, YuanConfig.K_BAN_LIST, true);
        boolean persistent = YuanConfig.get(cfg, YuanConfig.K_BAN_PERSIST, true);
        boolean mark = YuanConfig.get(cfg, YuanConfig.K_BAN_MARK, true);
        boolean drops = YuanConfig.get(cfg, YuanConfig.K_OBLIVION_DROP, false);
        boolean death = YuanConfig.get(cfg, YuanConfig.K_OBLIVION_DEATH, false);

        if (ban) {
            if (persistent && player.getServer() != null) YuanBanData.get(player.getServer()).addPersistent(le.getUUID());
            else YuanBanData.addSession(le.getUUID());
        }
        if (mark) le.getPersistentData().putBoolean("YuanErased", true);

        if (!death) {
            le.remove(Entity.RemovalReason.KILLED);
            return;
        }
        boolean suppressDrops = shouldSuppressDrops(drops, true);
        if (suppressDrops) markNoDrops(le.getUUID());
        le.invulnerableTime = 0;
        le.hurt(le.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
        if (le.isAlive()) {
            le.setHealth(0);
            le.die(player.damageSources().playerAttack(player));
        }
        if (shouldRemoveAfterNormalKill(le.isAlive(), le instanceof EnderDragon)) {
            if (suppressDrops) cancelNoDrops(le.getUUID());
            le.remove(Entity.RemovalReason.KILLED);
        }
    }

    public static void absoluteErase(Entity entity, Player player, ItemStack cfg) {
        if (entity instanceof PartEntity<?> part) entity = part.getParent();
        if (entity == player || entity.level().isClientSide || !(entity instanceof LivingEntity le)) return;
        int reentry = YuanConfig.getInt(cfg, YuanConfig.K_ABSOLUTE_REENTRY, 0);
        if (shouldApplyReentry(reentry)) {
            if (reentry == 2 && player.getServer() != null) YuanBanData.get(player.getServer()).addPersistent(le.getUUID());
            else YuanBanData.addSession(le.getUUID());
        }
        boolean suppressDrops = shouldSuppressDrops(YuanConfig.get(cfg, YuanConfig.K_ABSOLUTE_DROP, false), true);
        if (suppressDrops) markNoDrops(le.getUUID());
        le.setAbsorptionAmount(0);
        le.invulnerableTime = 0;
        le.hurt(player.damageSources().playerAttack(player), Float.MAX_VALUE);
        if (le.isAlive()) le.hurt(le.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
        if (le.isAlive()) le.hurt(le.damageSources().magic(), Float.MAX_VALUE);
        if (shouldUseExplicitDeathFallback(le.isAlive(), le instanceof EnderDragon)) {
            le.setHealth(0);
            le.die(player.damageSources().playerAttack(player));
        }
        if (shouldRemoveAfterNormalKill(le.isAlive(), le instanceof EnderDragon)) {
            if (suppressDrops) cancelNoDrops(le.getUUID());
            le.remove(Entity.RemovalReason.KILLED);
        }
        if (le instanceof EnderDragon dragon
                && shouldScheduleDragonFallback(YuanSwordItem.AttackMode.ABSOLUTE, dragon.isAlive()))
            queueDragonFallback(dragon);
    }

    static boolean shouldApplyReentry(int reentry) { return reentry > 0; }

    static boolean shouldSuppressDrops(boolean drops, boolean deathFlow) { return !drops && deathFlow; }

    static boolean shouldScheduleDragonFallback(YuanSwordItem.AttackMode mode, boolean alive) {
        return mode == YuanSwordItem.AttackMode.ABSOLUTE && alive;
    }

    static boolean shouldRemovePendingDragon(boolean alive, boolean deathProgressing,
                                             boolean graceRemaining, int ticks) {
        return alive && !deathProgressing && !graceRemaining && ticks <= 0;
    }

    public static void markNoDrops(UUID id) { markNoDrops(id, System.nanoTime()); }

    static void markNoDrops(UUID id, long now) {
        NO_DROPS.put(id, now + NO_DROPS_TTL_NANOS);
    }

    public static boolean consumeNoDrops(UUID id) { return consumeNoDrops(id, System.nanoTime()); }

    public static void cancelNoDrops(UUID id) { NO_DROPS.remove(id); }

    public static void cleanupNoDrops() { cleanupNoDrops(System.nanoTime()); }

    static void cleanupNoDrops(long now) {
        NO_DROPS.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    static boolean consumeNoDrops(UUID id, long now) {
        Long expires = NO_DROPS.remove(id);
        if (expires != null && expires > now) return true;
        cleanupNoDrops(now);
        return false;
    }

    static void queueDragonFallback(EnderDragon dragon) {
        if (PENDING_DRAGONS.size() >= MAX_PENDING_DRAGONS) {
            UUID oldest = PENDING_DRAGONS.keySet().stream().findFirst().orElse(null);
            if (oldest != null) PENDING_DRAGONS.remove(oldest);
        }
        PENDING_DRAGONS.put(dragon.getUUID(), DRAGON_GRACE_TICKS);
    }

    public static void tickPendingDragonFallbacks(MinecraftServer server) {
        PENDING_DRAGONS.replaceAll((id, ticks) -> ticks - 1);
        PENDING_DRAGONS.entrySet().removeIf(entry -> {
            EnderDragon dragon = null;
            for (net.minecraft.server.level.ServerLevel level : server.getAllLevels()) {
                Entity entity = level.getEntity(entry.getKey());
                if (entity instanceof EnderDragon found) { dragon = found; break; }
            }
            if (dragon == null || !dragon.isAlive() || dragon.deathTime > 0) return true;
            if (!shouldRemovePendingDragon(true, false, entry.getValue() > 0, entry.getValue())) return false;
            dragon.remove(Entity.RemovalReason.KILLED);
            return true;
        });
    }

    public static void chainLightning(Level level, Entity target) {
        visualLightning(level, target, 1, 0, true);
    }

    public static void visualLightning(Level level, Entity target, int count, float spread, boolean sound) {
        if (level.isClientSide) return;
        for (int i = 0; i < count; i++) {
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
            double angle = Math.random() * Math.PI * 2;
            double distance = lightningOffset(spread, Math.random());
            bolt.moveTo(target.getX() + Math.cos(angle) * distance, target.getY(),
                    target.getZ() + Math.sin(angle) * distance);
            bolt.setVisualOnly(true);
            bolt.setSilent(!sound);
            level.addFreshEntity(bolt);
        }
    }

    static double lightningOffset(float spread, double unit) {
        return spread * unit;
    }
}
