package com.yuan.item;

import net.minecraft.world.entity.Entity;

import java.util.IdentityHashMap;
import java.util.Map;

public final class YuanAbsoluteAttack {
    private static final ThreadLocal<Map<Object, Integer>> ACTIVE = ThreadLocal.withInitial(IdentityHashMap::new);

    private YuanAbsoluteAttack() {}

    public static Scope enter(Entity entity) {
        ACTIVE.get().merge(entity, 1, Integer::sum);
        return new Scope(entity);
    }

    public static boolean isActive(Entity entity) {
        return ACTIVE.get().containsKey(entity);
    }

    public static final class Scope implements AutoCloseable {
        private final Entity entity;
        private boolean closed;

        private Scope(Entity entity) { this.entity = entity; }

        @Override
        public void close() {
            if (closed) return;
            Map<Object, Integer> active = ACTIVE.get();
            int depth = active.getOrDefault(entity, 1) - 1;
            if (depth == 0) active.remove(entity); else active.put(entity, depth);
            if (active.isEmpty()) ACTIVE.remove();
            closed = true;
        }
    }
}
