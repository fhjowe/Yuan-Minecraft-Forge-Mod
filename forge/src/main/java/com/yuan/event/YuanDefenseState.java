package com.yuan.event;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

public final class YuanDefenseState {
    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Map<UUID, Map<ResourceKey<Level>, Vec3>> SAFE_POSITIONS = new HashMap<>();
    private static final ThreadLocal<Map<Object, Integer>> HEALTH_DECREASE =
            ThreadLocal.withInitial(IdentityHashMap::new);
    private static final ThreadLocal<Integer> ADMIN_COMMAND = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> LIFECYCLE_REMOVAL = ThreadLocal.withInitial(() -> 0);

    private YuanDefenseState() {}

    public static int scopeSlot(int scope, boolean main, boolean offhand, boolean inventory) {
        if (main) return 0;
        if (scope >= 1 && offhand) return 1;
        return scope >= 2 && inventory ? 2 : -1;
    }

    public static boolean scopeAllows(int scope, int slot) {
        return slot >= 0 && scope >= slot;
    }

    public static boolean activeDefense(boolean invincible, boolean blockingEnabled, boolean usingYuan,
                                        boolean scopeAllowsUsedStack, boolean absoluteAttack) {
        return !absoluteAttack && (invincible || blockingEnabled && usingYuan && scopeAllowsUsedStack);
    }

    public static float recoveryHealth(float current, float trusted, float maximum) {
        if (validHealth(trusted)) return validHealth(current) ? Math.max(current, trusted) : trusted;
        if (validHealth(current)) return current;
        return validHealth(maximum) ? maximum : 1;
    }

    public static boolean validHealth(float health) {
        return Float.isFinite(health) && health > 0;
    }

    public static boolean allowsDefense(boolean configured, boolean absoluteAttack) {
        return configured && !absoluteAttack;
    }

    public static boolean shouldBlockHealthSet(boolean serverSide, boolean defended, boolean absoluteAttack,
                                               float current, float requested) {
        return shouldBlockHealthSet(serverSide, defended, absoluteAttack, false, current, requested);
    }

    public static boolean shouldBlockHealthSet(boolean serverSide, boolean defended, boolean absoluteAttack,
                                               boolean allowedDecrease, float current, float requested) {
        return serverSide && defended && !absoluteAttack && !allowedDecrease
                && (!Float.isFinite(requested) || requested < current);
    }

    public static boolean shouldBlockDamage(boolean serverSide, boolean defended, boolean absoluteAttack,
                                            boolean generic, boolean hunger, boolean suffocation, int source) {
        if (!serverSide || !defended || absoluteAttack) return false;
        return source == 1 ? hunger : source == 2 ? suffocation : generic;
    }

    public static boolean protectRemoval(boolean serverSide, boolean configured, boolean adminBypassEnabled,
                                         boolean absoluteAttack, boolean administrativeCommand,
                                         boolean lifecycleRemoval, Entity.RemovalReason reason) {
        if (!serverSide || !configured || absoluteAttack || lifecycleRemoval) return false;
        if (administrativeCommand) return false;
        return reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED;
    }

    public static boolean protectRemoval(boolean configured, boolean absoluteAttack, Entity.RemovalReason reason) {
        return protectRemoval(true, configured, false, absoluteAttack, false, false, reason);
    }

    public static boolean protectRemoval(boolean configured, boolean absoluteAttack,
                                         Entity.RemovalReason reason, boolean lifecycleOrAdmin) {
        return protectRemoval(true, configured, true, absoluteAttack, lifecycleOrAdmin, lifecycleOrAdmin, reason);
    }

    public static Scope allowHealthDecrease(Object entity) {
        Map<Object, Integer> depths = HEALTH_DECREASE.get();
        depths.merge(entity, 1, Integer::sum);
        return new Scope(() -> {
            int depth = depths.getOrDefault(entity, 1) - 1;
            if (depth == 0) depths.remove(entity); else depths.put(entity, depth);
            if (depths.isEmpty()) HEALTH_DECREASE.remove();
        });
    }

    public static void authorizeHealthDecrease(Object entity) {
        HEALTH_DECREASE.get().merge(entity, 1, Integer::sum);
    }

    public static boolean consumeHealthDecrease(Object entity) {
        Map<Object, Integer> depths = HEALTH_DECREASE.get();
        int depth = depths.getOrDefault(entity, 0);
        if (depth == 0) return false;
        if (depth == 1) depths.remove(entity); else depths.put(entity, depth - 1);
        if (depths.isEmpty()) HEALTH_DECREASE.remove();
        return true;
    }

    public static Scope enterAdministrativeCommand() { return enter(ADMIN_COMMAND); }

    public static Scope enterLifecycleRemoval() { return enter(LIFECYCLE_REMOVAL); }

    public static boolean healthDecreaseAllowed(Object entity) { return HEALTH_DECREASE.get().containsKey(entity); }

    public static boolean administrativeCommand() { return ADMIN_COMMAND.get() > 0; }

    public static boolean lifecycleRemoval() { return LIFECYCLE_REMOVAL.get() > 0; }

    public static boolean safePositionDecision(boolean support, boolean collisionFree, boolean headroom,
                                               boolean fluidFree, boolean insideBorder, boolean insideBuildBounds) {
        return support && collisionFree && headroom && fluidFree && insideBorder && insideBuildBounds;
    }

    public static boolean supportedRescuePosition(boolean support, boolean collisionFree, boolean headroom,
                                                  boolean fluidFree, boolean insideBorder,
                                                  boolean insideBuildBounds, boolean loaded) {
        return loaded && safePositionDecision(support, collisionFree, headroom, fluidFree,
                insideBorder, insideBuildBounds);
    }

    public static boolean suspensionPosition(boolean collisionFree, boolean headroom, boolean fluidFree,
                                             boolean insideBorder, boolean insideBuildBounds, boolean loaded) {
        return loaded && collisionFree && headroom && fluidFree && insideBorder && insideBuildBounds;
    }

    public static int voidFallback(boolean hasSafePosition, boolean hasRespawnPosition) {
        return hasSafePosition ? 0 : hasRespawnPosition ? 1 : 2;
    }

    public static int rescueY(int requestedY, int minBuildHeight) {
        return Math.max(requestedY, minBuildHeight + 1);
    }

    public enum RescueState { RESCUED, HOLD_RETRY, PHASE_HOLD }

    public record RescueDecision(RescueState state, int y, boolean targetAvailable, boolean retry,
                                 boolean enableNoGravity, boolean ownsNoGravity, boolean enableNoPhysics,
                                 boolean ownsNoPhysics, boolean zeroVelocity) {}

    public record VoidCleanup(boolean clearRetry, boolean clearNoGravity, boolean clearNoPhysics) {}

    public record EmergencyTarget(double x, int y, double z) {}

    public static RescueDecision terminalRescueDecision(boolean supportedDestination, boolean targetAvailable,
                                                        boolean noGravity, boolean noPhysics,
                                                        int requestedY, int minBuildHeight) {
        boolean retry = !supportedDestination;
        boolean enableNoGravity = retry && !noGravity;
        boolean phase = retry && !targetAvailable;
        boolean enableNoPhysics = phase && !noPhysics;
        RescueState state = supportedDestination ? RescueState.RESCUED
                : targetAvailable ? RescueState.HOLD_RETRY : RescueState.PHASE_HOLD;
        return new RescueDecision(state, rescueY(requestedY, minBuildHeight), true, retry,
                enableNoGravity, enableNoGravity, enableNoPhysics, enableNoPhysics, true);
    }

    public static VoidCleanup cleanupVoidState(boolean retry, boolean ownsNoGravity, boolean ownsNoPhysics) {
        return new VoidCleanup(retry, ownsNoGravity, ownsNoPhysics);
    }

    public static EmergencyTarget emergencyTarget(double currentX, double currentZ,
                                                  double spawnX, double spawnZ,
                                                  double borderMin, double borderMax,
                                                  int minBuildHeight, int maxBuildHeight) {
        return emergencyTarget(currentX, currentZ, spawnX, spawnZ, borderMin, borderMax,
                borderMin, borderMax, minBuildHeight, maxBuildHeight);
    }

    public static EmergencyTarget emergencyTarget(double currentX, double currentZ,
                                                  double spawnX, double spawnZ,
                                                  double minX, double maxX, double minZ, double maxZ,
                                                  int minBuildHeight, int maxBuildHeight) {
        double x = Double.isFinite(currentX) ? currentX : spawnX;
        double z = Double.isFinite(currentZ) ? currentZ : spawnZ;
        if (!Double.isFinite(x)) x = 0;
        if (!Double.isFinite(z)) z = 0;
        double inset = 1.0E-3;
        x = Math.max(minX + inset, Math.min(maxX - inset, x));
        z = Math.max(minZ + inset, Math.min(maxZ - inset, z));
        int y = Math.min(Math.max(minBuildHeight + 1, minBuildHeight + 1), maxBuildHeight - 2);
        return new EmergencyTarget(x, y, z);
    }

    public static void updateSession(UUID id, boolean active, float health, float maximum, float absorption) {
        if (!active) {
            reset(id);
            return;
        }
        Session session = SESSIONS.computeIfAbsent(id,
                ignored -> new Session(recoveryHealth(health, Float.NaN, maximum), validAbsorption(absorption)));
        if (validHealth(health)) session.health = Math.max(session.health, health);
        if (Float.isFinite(absorption)) session.absorption = Math.max(session.absorption, validAbsorption(absorption));
    }

    public static float healthBaseline(UUID id) {
        Session session = SESSIONS.get(id);
        return session == null ? Float.NaN : session.health;
    }

    public static float absorptionBaseline(UUID id) {
        Session session = SESSIONS.get(id);
        return session == null ? 0 : session.absorption;
    }

    public static float trustedHealth(ServerPlayer player) { return healthBaseline(player.getUUID()); }

    public static float trustedAbsorption(ServerPlayer player) { return absorptionBaseline(player.getUUID()); }

    public static void recordSafePosition(ServerPlayer player) {
        SAFE_POSITIONS.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>())
                .put(player.level().dimension(), player.position());
    }

    public static Vec3 safePosition(ServerPlayer player) {
        Map<ResourceKey<Level>, Vec3> positions = SAFE_POSITIONS.get(player.getUUID());
        return positions == null ? null : positions.get(player.level().dimension());
    }

    public static void forgetSafePosition(ServerPlayer player) {
        Map<ResourceKey<Level>, Vec3> positions = SAFE_POSITIONS.get(player.getUUID());
        if (positions != null) positions.remove(player.level().dimension());
    }

    public static void reset(UUID id) {
        SESSIONS.remove(id);
        SAFE_POSITIONS.remove(id);
    }

    public static void clear(ServerPlayer player) { reset(player.getUUID()); }

    public static void clearAll() {
        SESSIONS.clear();
        SAFE_POSITIONS.clear();
    }

    private static float validAbsorption(float absorption) {
        return Float.isFinite(absorption) && absorption > 0 ? absorption : 0;
    }

    private static Scope enter(ThreadLocal<Integer> depth) {
        depth.set(depth.get() + 1);
        return new Scope(() -> {
            int value = depth.get() - 1;
            if (value == 0) depth.remove(); else depth.set(value);
        });
    }

    public static final class Scope implements AutoCloseable {
        private final Runnable close;
        private boolean closed;

        private Scope(Runnable close) { this.close = close; }

        @Override
        public void close() {
            if (closed) return;
            close.run();
            closed = true;
        }
    }

    private static final class Session {
        private float health;
        private float absorption;

        private Session(float health, float absorption) {
            this.health = health;
            this.absorption = absorption;
        }
    }
}
