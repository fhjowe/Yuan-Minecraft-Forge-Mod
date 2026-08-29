package com.yuan.timerewind;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class RewindHistory {
    private static final class Entry {
        final long tick;
        final Object event;
        Entry(long tick, Object event) { this.tick = tick; this.event = event; }
    }

    private final long windowTicks;
    private final ArrayDeque<Entry> entries = new ArrayDeque<>();

    public RewindHistory(long windowTicks) {
        this.windowTicks = Math.max(1L, windowTicks);
    }

    public synchronized void add(long tick, Object event) {
        entries.addLast(new Entry(tick, event));
        long cutoff = tick - windowTicks;
        while (!entries.isEmpty() && entries.peekFirst().tick < cutoff) {
            entries.removeFirst();
        }
    }

    public synchronized long earliestTick() {
        return entries.isEmpty() ? Long.MAX_VALUE : entries.peekFirst().tick;
    }

    public synchronized long windowTicks() {
        return windowTicks;
    }

    public synchronized List<Object> since(long targetTick) {
        List<Object> out = new ArrayList<>();
        for (Entry e : entries) {
            if (e.tick >= targetTick) out.add(e.event);
        }
        return out;
    }

    public synchronized List<Object> atOrBefore(long targetTick) {
        List<Object> out = new ArrayList<>();
        for (Entry e : entries) {
            if (e.tick <= targetTick) out.add(e.event);
        }
        return out;
    }

    public synchronized void clear() {
        entries.clear();
    }
}
