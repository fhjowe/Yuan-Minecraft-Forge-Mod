package com.yuan.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class YuanBanData extends SavedData {
    private static final String NAME = "yuan_bans";
    private static final Set<UUID> SESSION = new LinkedHashSet<>();
    private final Set<UUID> persistent = new LinkedHashSet<>();

    public static YuanBanData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(YuanBanData::load, YuanBanData::new, NAME);
    }

    public static synchronized boolean addSession(UUID id) { return SESSION.add(id); }
    public static synchronized boolean removeSession(UUID id) { return SESSION.remove(id); }
    public static synchronized boolean isSessionBanned(UUID id) { return SESSION.contains(id); }
    public static synchronized Set<UUID> sessionEntries() { return Set.copyOf(SESSION); }
    public static synchronized void clearSession() { SESSION.clear(); }

    public boolean addPersistent(UUID id) {
        if (!persistent.add(id)) return false;
        setDirty();
        return true;
    }

    public boolean removePersistent(UUID id) {
        if (!persistent.remove(id)) return false;
        setDirty();
        return true;
    }

    public boolean isPersistentBanned(UUID id) { return persistent.contains(id); }
    public Set<UUID> persistentEntries() { return Set.copyOf(persistent); }

    public void clearPersistent() {
        if (persistent.isEmpty()) return;
        persistent.clear();
        setDirty();
    }

    public static boolean isBanned(MinecraftServer server, UUID id) {
        return isSessionBanned(id) || server != null && get(server).isPersistentBanned(id);
    }

    public static boolean remove(MinecraftServer server, UUID id) {
        boolean removed = removeSession(id);
        return server != null && get(server).removePersistent(id) || removed;
    }

    public static YuanBanData load(CompoundTag tag) {
        YuanBanData data = new YuanBanData();
        ListTag list = tag.getList("UUIDs", Tag.TAG_STRING);
        for (Tag entry : list) {
            try { data.persistent.add(UUID.fromString(entry.getAsString())); }
            catch (IllegalArgumentException ignored) {}
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (UUID id : persistent) list.add(StringTag.valueOf(id.toString()));
        tag.put("UUIDs", list);
        return tag;
    }
}
