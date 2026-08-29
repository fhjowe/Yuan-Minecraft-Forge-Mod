package com.yuan.client.gui;

import net.minecraft.nbt.CompoundTag;
import java.util.HashSet;
import java.util.Set;

public final class YuanSaveState {
    public enum Status { IDLE, PENDING, SYNCING, SAVED, FAILED }

    private long nextRequest;
    private long latestRequest;
    private long latestAcknowledged;
    private Status status = Status.IDLE;
    private CompoundTag applied = new CompoundTag();
    private String message = "";
    private final Set<Long> pendingExplicit = new HashSet<>();

    public long begin(boolean optimistic) {
        latestRequest = ++nextRequest;
        status = optimistic ? Status.PENDING : Status.SYNCING;
        if (!optimistic) pendingExplicit.add(latestRequest);
        message = "";
        return latestRequest;
    }

    public boolean acknowledge(long requestId, boolean success, CompoundTag config, String message) {
        if (requestId <= latestAcknowledged) return false;
        latestAcknowledged = requestId;
        pendingExplicit.remove(requestId);
        applied = config == null ? new CompoundTag() : config.copy();
        this.message = message == null ? "" : message;
        status = pendingExplicit.isEmpty() ? (success ? Status.SAVED : Status.FAILED) : Status.SYNCING;
        return true;
    }

    public Status status() { return status; }
    public CompoundTag applied() { return applied.copy(); }
    public String message() { return message; }
    public long latestRequest() { return latestRequest; }
    public boolean hasPendingExplicit() { return !pendingExplicit.isEmpty(); }
}
