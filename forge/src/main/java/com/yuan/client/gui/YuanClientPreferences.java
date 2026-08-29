package com.yuan.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.EnumSet;

public final class YuanClientPreferences {
    public enum AnimationPreset { OFF, LIGHT, SMOOTH, ARTIFACT }
    public enum AnimationCategory { PANELS, LISTS, CONTROLS, VALUES, SAVE_STATUS, PREVIEW, DANGER }
    public enum AnimationIntensity { LOW, MEDIUM, HIGH }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public YuanConfigCatalog.Detail detail = YuanConfigCatalog.Detail.PRACTICAL;
    public boolean autoSave;
    public Set<String> favorites = new HashSet<>();
    public String lastCategory = "攻击";
    public String search = "";
    public boolean modifiedOnly;
    public boolean sidebarCollapsed;
    public String lastPreset = "";
    public AnimationPreset animationPreset = AnimationPreset.SMOOTH;
    public boolean animations = true;
    public Set<AnimationCategory> animationCategories = defaults(AnimationPreset.SMOOTH);
    public float animationSpeed = 1.0f;
    public AnimationIntensity animationIntensity = AnimationIntensity.MEDIUM;
    public boolean reduceMotion;

    public static YuanClientPreferences load() {
        try {
            Path path = path();
            return Files.exists(path) ? fromJson(Files.readString(path)) : new YuanClientPreferences();
        } catch (Exception ignored) {
            return new YuanClientPreferences();
        }
    }

    public boolean save() { return save(path()); }

    boolean save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, toJson());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean saveCheck(YuanClientPreferences preferences, boolean writable) {
        try {
            Path root = Files.createTempDirectory("yuan-preferences");
            Path target = writable ? root.resolve("ui.json") : Files.createFile(root.resolve("blocked")).resolve("ui.json");
            return preferences.save(target);
        } catch (IOException ignored) {
            return false;
        }
    }

    public String toJson() { return GSON.toJson(this); }

    public static YuanClientPreferences fromJson(String json) {
        try {
            YuanClientPreferences value = GSON.fromJson(json, YuanClientPreferences.class);
            if (value == null) return new YuanClientPreferences();
            if (value.detail == null) value.detail = YuanConfigCatalog.Detail.PRACTICAL;
            if (value.favorites == null) value.favorites = new HashSet<>();
            if (value.lastCategory == null) value.lastCategory = "攻击";
            if (value.search == null) value.search = "";
            if (value.lastPreset == null) value.lastPreset = "";
            if (value.animationPreset == null) value.animationPreset = AnimationPreset.SMOOTH;
            if (value.animationCategories == null) value.animationCategories = defaults(value.animationPreset);
            if (value.animationIntensity == null) value.animationIntensity = AnimationIntensity.MEDIUM;
            if (value.animationSpeed != .5f && value.animationSpeed != .75f && value.animationSpeed != 1f
                    && value.animationSpeed != 1.25f && value.animationSpeed != 1.5f) value.animationSpeed = 1f;
            return value;
        } catch (Exception ignored) {
            return new YuanClientPreferences();
        }
    }

    public void applyAnimationPreset(AnimationPreset preset) {
        animationPreset = preset;
        animations = preset != AnimationPreset.OFF;
        animationCategories = defaults(preset);
    }

    public boolean animationEnabled(AnimationCategory category) {
        if (!animations || animationPreset == AnimationPreset.OFF) return false;
        if (reduceMotion) return category == AnimationCategory.CONTROLS || category == AnimationCategory.SAVE_STATUS;
        return animationCategories.contains(category);
    }

    public long animationDuration(long smoothMilliseconds) {
        if (!animations) return 0;
        float presetFactor = switch (animationPreset) {
            case OFF -> 0;
            case LIGHT -> .65f;
            case SMOOTH -> 1;
            case ARTIFACT -> 1.55f;
        };
        return Math.max(0, Math.round(smoothMilliseconds * presetFactor / animationSpeed));
    }

    private static Set<AnimationCategory> defaults(AnimationPreset preset) {
        return switch (preset) {
            case OFF -> EnumSet.noneOf(AnimationCategory.class);
            case LIGHT -> EnumSet.of(AnimationCategory.PANELS, AnimationCategory.CONTROLS,
                    AnimationCategory.SAVE_STATUS);
            case SMOOTH -> EnumSet.allOf(AnimationCategory.class);
            case ARTIFACT -> EnumSet.allOf(AnimationCategory.class);
        };
    }

    private static Path path() {
        Path config = FMLPaths.CONFIGDIR.get();
        if (config == null) config = Path.of("config");
        return config.resolve("yuan/ui.json");
    }
}
