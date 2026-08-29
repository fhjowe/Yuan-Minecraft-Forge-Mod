package com.yuan.data;

import java.util.List;
import java.util.UUID;

public final class YuanBanClientState {
    private static List<UUID> session = List.of();
    private static List<UUID> persistent = List.of();
    private static int revision;

    public static void update(List<UUID> sessionEntries, List<UUID> persistentEntries) {
        session = List.copyOf(sessionEntries);
        persistent = List.copyOf(persistentEntries);
        revision++;
    }

    public static List<UUID> session() { return session; }
    public static List<UUID> persistent() { return persistent; }
    public static int revision() { return revision; }
}
