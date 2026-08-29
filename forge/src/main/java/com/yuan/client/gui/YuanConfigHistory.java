package com.yuan.client.gui;

import com.yuan.item.YuanConfig;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class YuanConfigHistory {
    private static final int LIMIT = 100;
    private CompoundTag original;
    private CompoundTag draft;
    private final Deque<CompoundTag> undo = new ArrayDeque<>();
    private final Deque<CompoundTag> redo = new ArrayDeque<>();

    public YuanConfigHistory(CompoundTag original) {
        this.original = mergeSanitized(original);
        this.draft = this.original.copy();
    }

    public CompoundTag draft() { return draft.copy(); }
    public CompoundTag original() { return original.copy(); }
    public boolean canUndo() { return !undo.isEmpty(); }
    public boolean canRedo() { return !redo.isEmpty(); }
    public boolean isDirty() { return !changedKeys().isEmpty(); }

    public void setBoolean(String key, boolean value) {
        edit(tag -> tag.putBoolean(key, value));
    }

    public void setFloat(String key, float value) {
        YuanConfigCatalog.Setting setting = YuanConfigCatalog.byKey(key);
        float clamped = setting == null ? value : Math.max(setting.min(), Math.min(setting.max(), value));
        edit(tag -> tag.putFloat(key, clamped));
    }

    public void setInt(String key, int value) {
        YuanConfigCatalog.Setting setting = YuanConfigCatalog.byKey(key);
        int clamped = setting == null ? value : Math.max((int)setting.min(), Math.min((int)setting.max(), value));
        edit(tag -> tag.putInt(key, clamped));
    }

    public void replace(CompoundTag value) {
        CompoundTag next = mergeSanitized(value);
        if (draft.equals(next)) return;
        checkpoint();
        draft = next;
    }

    public void edit(Consumer<CompoundTag> change) {
        CompoundTag next = draft.copy();
        change.accept(next);
        next = mergeSanitized(next);
        if (draft.equals(next)) return;
        checkpoint();
        draft = next;
    }

    public void markSaved() { original = draft.copy(); }
    public void markSaved(CompoundTag applied) { original = mergeSanitized(applied); }

    public void reconcile(CompoundTag applied) {
        draft = mergeSanitized(applied);
        original = draft.copy();
    }

    public void undo() {
        if (undo.isEmpty()) return;
        redo.push(draft.copy());
        draft = undo.pop();
    }

    public void redo() {
        if (redo.isEmpty()) return;
        undo.push(draft.copy());
        draft = redo.pop();
    }

    public Set<String> changedKeys() {
        Set<String> changed = new HashSet<>();
        for (YuanConfigCatalog.Setting setting : YuanConfigCatalog.all()) {
            if (!same(original, draft, setting)) changed.add(setting.key());
        }
        return changed;
    }

    public void resetCategory(String category) {
        edit(tag -> {
            for (YuanConfigCatalog.Setting setting : YuanConfigCatalog.all()) {
                if (!setting.category().equals(category)) continue;
                if (setting.kind() == YuanConfigCatalog.Kind.BOOLEAN)
                    tag.putBoolean(setting.key(), setting.defaultValue() != 0);
                else if (setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER)
                    tag.putInt(setting.key(), (int)setting.defaultValue());
                else tag.putFloat(setting.key(), setting.defaultValue());
            }
        });
    }

    public void resetGroup(String group) {
        edit(tag -> {
            for (YuanConfigCatalog.Setting setting : YuanConfigCatalog.groupSettings(group)) {
                if (setting.kind() == YuanConfigCatalog.Kind.BOOLEAN)
                    tag.putBoolean(setting.key(), setting.defaultValue() != 0);
                else if (setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER)
                    tag.putInt(setting.key(), (int)setting.defaultValue());
                else tag.putFloat(setting.key(), setting.defaultValue());
            }
        });
    }

    private void checkpoint() {
        undo.push(draft.copy());
        while (undo.size() > LIMIT) undo.removeLast();
        redo.clear();
    }

    private static boolean same(CompoundTag a, CompoundTag b, YuanConfigCatalog.Setting setting) {
        if (setting.kind() == YuanConfigCatalog.Kind.BOOLEAN)
            return getBoolean(a, setting) == getBoolean(b, setting);
        if (setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER)
            return getInt(a, setting) == getInt(b, setting);
        return Float.compare(getFloat(a, setting), getFloat(b, setting)) == 0;
    }

    private static boolean getBoolean(CompoundTag tag, YuanConfigCatalog.Setting setting) {
        return tag.contains(setting.key()) ? tag.getBoolean(setting.key()) : setting.defaultValue() != 0;
    }

    private static float getFloat(CompoundTag tag, YuanConfigCatalog.Setting setting) {
        return tag.contains(setting.key()) ? tag.getFloat(setting.key()) : setting.defaultValue();
    }

    private static int getInt(CompoundTag tag, YuanConfigCatalog.Setting setting) {
        return tag.contains(setting.key()) ? tag.getInt(setting.key()) : (int)setting.defaultValue();
    }

    private static CompoundTag mergeSanitized(CompoundTag value) {
        return YuanConfig.sanitize(value);
    }
}
