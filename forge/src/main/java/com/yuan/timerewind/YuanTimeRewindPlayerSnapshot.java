package com.yuan.timerewind;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public final class YuanTimeRewindPlayerSnapshot {
    private YuanTimeRewindPlayerSnapshot() {}

    public static CompoundTag capture(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        player.saveWithoutId(tag);
        return tag;
    }

    public static void restore(ServerPlayer player, CompoundTag tag) {
        restore(player, tag, true);
    }

    public static void restore(ServerPlayer player, CompoundTag tag, boolean restorePosition) {
        CompoundTag effective = effectiveRestoreTag(tag, restorePosition);
        if (effective == null) return;
        player.load(effective);
    }

    static CompoundTag effectiveRestoreTag(CompoundTag tag, boolean restorePosition) {
        if (tag == null) return null;
        CompoundTag effective = tag;
        if (!restorePosition) {
            effective = tag.copy();
            effective.remove("Pos");
            effective.remove("Motion");
            effective.remove("Rotation");
        }
        return effective;
    }
}
