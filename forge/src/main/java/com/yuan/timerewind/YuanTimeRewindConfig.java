package com.yuan.timerewind;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Server-side (COMMON) tuning for the god sword time rewind recorder.
 *
 * recordScanIntervalTicks controls how often the server rescans every online
 * player's inventory to discover enabled rewind swords and derive the maximum
 * recording window. It used to run every tick; a longer interval removes that
 * fixed per-tick cost. A newly enabled/disabled sword takes effect within this
 * many ticks.
 *
 * containerSnapshotIntervalTicks controls how often container-like block
 * entities (chests, barrels, furnaces, ...) are diffed against their last
 * known NBT so container deltas are tracked at finer granularity than the
 * once-per-second world snapshot.
 */
@Mod.EventBusSubscriber(modid = "yuan", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class YuanTimeRewindConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.IntValue RECORD_SCAN_INTERVAL = BUILDER
            .comment("Ticks between rescans for enabled rewind swords / recording window (1 = every tick, 20 = 1 second)")
            .defineInRange("recordScanIntervalTicks", 20, 1, 1200);
    private static final ForgeConfigSpec.IntValue CONTAINER_SNAPSHOT_INTERVAL = BUILDER
            .comment("Ticks between container block-entity snapshots for anti-dupe delta tracking (3 = 0.15s, 20 = 1s)")
            .defineInRange("containerSnapshotIntervalTicks", 3, 1, 100);
    private static final ForgeConfigSpec.IntValue ENTITY_SNAPSHOT_INTERVAL = BUILDER
            .comment("Ticks between entity/player snapshots in the rewind history (5 = 0.25s; larger = less memory, coarser restore)")
            .defineInRange("entitySnapshotIntervalTicks", 5, 1, 100);
    private static final ForgeConfigSpec.IntValue WORLD_SNAPSHOT_INTERVAL = BUILDER
            .comment("Ticks between world-state (time/weather/raids) snapshots (10 = 0.5s; smaller = more precise time restore)")
            .defineInRange("worldSnapshotIntervalTicks", 10, 1, 200);
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static volatile int recordScanIntervalTicks = 20;
    public static volatile int containerSnapshotIntervalTicks = 3;
    public static volatile int entitySnapshotIntervalTicks = 5;
    public static volatile int worldSnapshotIntervalTicks = 10;

    private YuanTimeRewindConfig() {
    }

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        recordScanIntervalTicks = RECORD_SCAN_INTERVAL.get();
        containerSnapshotIntervalTicks = CONTAINER_SNAPSHOT_INTERVAL.get();
        entitySnapshotIntervalTicks = ENTITY_SNAPSHOT_INTERVAL.get();
        worldSnapshotIntervalTicks = WORLD_SNAPSHOT_INTERVAL.get();
    }
}
