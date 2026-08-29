package com.yuan.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuan.item.YuanConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class YuanPresetManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record ImportResult(CompoundTag config, Set<String> unknownKeys,
                               Set<String> changedKeys, Set<String> clampedKeys,
                               Set<String> dangerousKeys, int preservedCount) {}

    private YuanPresetManager() {}

    public static String toJson(CompoundTag config) {
        JsonObject root = new JsonObject();
        root.addProperty("format", 1);
        JsonObject values = new JsonObject();
        for (YuanConfigCatalog.Setting setting : YuanConfigCatalog.all()) {
            if (setting.kind() == YuanConfigCatalog.Kind.BOOLEAN)
                values.addProperty(setting.key(), config.contains(setting.key())
                        ? config.getBoolean(setting.key()) : setting.defaultValue() != 0);
            else values.addProperty(setting.key(), config.contains(setting.key())
                    ? config.getFloat(setting.key()) : setting.defaultValue());
        }
        root.add("values", values);
        return GSON.toJson(root);
    }

    public static ImportResult parse(String json, CompoundTag base) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (root.has("format") && root.get("format").getAsInt() != 1)
            throw new IllegalArgumentException("Unsupported preset format");
        if (root.has("values") && !root.get("values").isJsonObject())
            throw new IllegalArgumentException("Preset values must be an object");
        JsonObject values = root.has("values") ? root.getAsJsonObject("values") : root;
        CompoundTag raw = base == null ? new CompoundTag() : base.copy();
        Set<String> importedKeys = new HashSet<>();
        Set<String> unknown = new HashSet<>();
        for (var entry : values.entrySet()) {
            YuanConfigCatalog.Setting setting = YuanConfigCatalog.byKey(entry.getKey());
            if (setting == null) {
                unknown.add(entry.getKey());
                continue;
            }
            importedKeys.add(setting.key());
            JsonElement value = entry.getValue();
            if (setting.kind() == YuanConfigCatalog.Kind.BOOLEAN) {
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean())
                    throw new IllegalArgumentException(setting.key() + " must be boolean");
                raw.putBoolean(setting.key(), value.getAsBoolean());
            } else {
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
                    throw new IllegalArgumentException(setting.key() + " must be number");
                raw.putFloat(setting.key(), value.getAsFloat());
            }
        }
        CompoundTag sanitizedKnown = YuanConfig.sanitize(raw);
        CompoundTag clean = raw.copy();
        for (String key : sanitizedKnown.getAllKeys()) clean.put(key, sanitizedKnown.get(key).copy());
        Set<String> changed = new HashSet<>();
        Set<String> clamped = new HashSet<>();
        Set<String> dangerous = new HashSet<>();
        for (YuanConfigCatalog.Setting setting : YuanConfigCatalog.all()) {
            if (!importedKeys.contains(setting.key())) continue;
            if (setting.kind() == YuanConfigCatalog.Kind.BOOLEAN) {
                if (raw.getBoolean(setting.key()) != getBoolean(base, setting)) changed.add(setting.key());
                if (setting.dangerous() && raw.getBoolean(setting.key()) && !getBoolean(base, setting))
                    dangerous.add(setting.key());
            } else {
                float rawValue = raw.getFloat(setting.key());
                float cleanValue = clean.getFloat(setting.key());
                if (Float.compare(rawValue, cleanValue) != 0) clamped.add(setting.key());
                if (Float.compare(cleanValue, getFloat(base, setting)) != 0) changed.add(setting.key());
            }
        }
        return new ImportResult(clean, unknown, changed, clamped, dangerous,
                Math.max(0, YuanConfigCatalog.all().size() - importedKeys.size()));
    }

    public static List<String> list() {
        try {
            Path directory = directory();
            Files.createDirectories(directory);
            try (var files = Files.list(directory)) {
                return files.filter(path -> path.getFileName().toString().endsWith(".json"))
                        .map(path -> path.getFileName().toString().replaceFirst("\\.json$", ""))
                        .sorted().toList();
            }
        } catch (IOException ignored) {
            return List.of();
        }
    }

    public static void save(String name, CompoundTag config) throws IOException {
        Files.createDirectories(directory());
        Path target = path(name);
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(temporary, toJson(config));
        try { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static ImportResult load(String name, CompoundTag base) throws IOException {
        return parse(Files.readString(path(name)), base);
    }

    public static void delete(String name) throws IOException { Files.deleteIfExists(path(name)); }

    public static void rename(String from, String to) throws IOException { Files.move(path(from), path(to)); }

    public static boolean exists(String name) { return Files.exists(path(name)); }

    public static String normalizedName(String name) {
        return path(name).getFileName().toString().replaceFirst("\\.json$", "");
    }

    private static Path path(String name) {
        String safe = name.replaceAll("[\\\\/:*?\"<>|]", "_").replace("..", "_").trim();
        if (safe.isBlank()) safe = "preset";
        return directory().resolve(safe + ".json");
    }

    private static Path directory() {
        Path config = FMLPaths.CONFIGDIR.get();
        if (config == null) config = Path.of("config");
        return config.resolve("yuan/presets");
    }

    private static boolean getBoolean(CompoundTag tag, YuanConfigCatalog.Setting setting) {
        return tag.contains(setting.key()) ? tag.getBoolean(setting.key()) : setting.defaultValue() != 0;
    }

    private static float getFloat(CompoundTag tag, YuanConfigCatalog.Setting setting) {
        return tag.contains(setting.key()) ? tag.getFloat(setting.key()) : setting.defaultValue();
    }
}
