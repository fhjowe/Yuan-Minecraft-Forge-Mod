package com.yuan.timestop;

public final class YuanTimeStopServerState {
    private static volatile boolean stopped;
    private static volatile boolean invulnerable = true;
    private static volatile long stopUntilMillis = 0L;
    private static volatile boolean freezeSelf = false;
    private static volatile boolean freezeEntities = true;
    private static volatile boolean freezeBlocks = true;
    private static volatile boolean freezeFluids = true;
    private static volatile boolean freezeBossAI = true;
    private static volatile float stopRadius = 0.0f;
    private static volatile double wielderX;
    private static volatile double wielderY;
    private static volatile double wielderZ;
    private static volatile long nextAllowedMillis = 0L;

    private YuanTimeStopServerState() {
    }

    public static boolean isStopped() {
        return stopped;
    }

    public static void setStopped(boolean value) {
        stopped = value;
    }

    public static boolean isInvulnerable() {
        return invulnerable;
    }

    public static void setInvulnerable(boolean value) {
        invulnerable = value;
    }

    public static long getStopUntilMillis() {
        return stopUntilMillis;
    }

    public static void setStopUntilMillis(long value) {
        stopUntilMillis = value;
    }

    public static boolean isFreezeSelf() {
        return freezeSelf;
    }

    public static void setFreezeSelf(boolean value) {
        freezeSelf = value;
    }

    public static boolean isFreezeEntities() {
        return freezeEntities;
    }

    public static void setFreezeEntities(boolean value) {
        freezeEntities = value;
    }

    public static boolean isFreezeBlocks() {
        return freezeBlocks;
    }

    public static void setFreezeBlocks(boolean value) {
        freezeBlocks = value;
    }

    public static boolean isFreezeFluids() {
        return freezeFluids;
    }

    public static void setFreezeFluids(boolean value) {
        freezeFluids = value;
    }

    public static boolean isFreezeBossAI() {
        return freezeBossAI;
    }

    public static void setFreezeBossAI(boolean value) {
        freezeBossAI = value;
    }

    public static float getStopRadius() {
        return stopRadius;
    }

    public static void setStopRadius(float value) {
        stopRadius = value;
    }

    public static double getWielderX() {
        return wielderX;
    }

    public static double getWielderY() {
        return wielderY;
    }

    public static double getWielderZ() {
        return wielderZ;
    }

    public static void setWielderPosition(double x, double y, double z) {
        wielderX = x;
        wielderY = y;
        wielderZ = z;
    }

    public static boolean cooldownReady(int ticks) {
        return ticks <= 0 || System.currentTimeMillis() >= nextAllowedMillis;
    }

    public static void startCooldown(int ticks) {
        nextAllowedMillis = System.currentTimeMillis() + (long) Math.max(0, ticks) * 50L;
    }

    public static void resetFreezeDefaults() {
        freezeSelf = false;
        freezeEntities = true;
        freezeBlocks = true;
        freezeFluids = true;
        freezeBossAI = true;
        stopRadius = 0.0f;
        wielderX = 0.0;
        wielderY = 0.0;
        wielderZ = 0.0;
    }
}
