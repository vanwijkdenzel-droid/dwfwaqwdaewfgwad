package dev.cpvp;

import com.google.gson.*;
import dev.cpvp.module.Module;
import dev.cpvp.module.ModuleManager;
import dev.cpvp.setting.BooleanSetting;
import dev.cpvp.setting.NumberSetting;
import dev.cpvp.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Saves / loads module states, keybinds and settings as JSON in .minecraft/config/fishv1/. */
public final class ProfileManager {
    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("fishv1");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static List<String> cache = new ArrayList<>(List.of("Default"));
    public static String current = "Default";

    private ProfileManager() {}

    public static void init() {
        try { Files.createDirectories(DIR); } catch (IOException ignored) {}
        if (!Files.exists(file("Default"))) save("Default");
        refresh();
    }

    public static List<String> names() { return cache; }

    private static Path file(String n) { return DIR.resolve(n + ".json"); }

    private static void refresh() {
        List<String> out = new ArrayList<>();
        try (Stream<Path> s = Files.list(DIR)) {
            s.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".json"))
                    .map(n -> n.substring(0, n.length() - 5)).sorted().forEach(out::add);
        } catch (IOException ignored) {}
        if (!out.contains("Default")) out.add(0, "Default");
        cache = out;
    }

    public static void saveCurrent() { save(current); }

    public static void save(String name) {
        JsonObject root = new JsonObject();
        for (Module m : ModuleManager.INSTANCE.all()) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.isEnabled());
            o.addProperty("key", m.getKey());
            JsonObject st = new JsonObject();
            for (Setting s : m.getSettings()) {
                if (s instanceof NumberSetting n) st.addProperty(s.getName(), n.get());
                else if (s instanceof BooleanSetting b) st.addProperty(s.getName(), b.get());
            }
            o.add("settings", st);
            root.add(m.getName(), o);
        }
        try { Files.writeString(file(name), GSON.toJson(root)); } catch (IOException ignored) {}
    }

    public static void load(String name) {
        try {
            JsonObject root = JsonParser.parseString(Files.readString(file(name))).getAsJsonObject();
            for (Module m : ModuleManager.INSTANCE.all()) {
                if (!root.has(m.getName())) continue;
                JsonObject o = root.getAsJsonObject(m.getName());
                if (o.has("key")) m.setKey(o.get("key").getAsInt());
                JsonObject st = o.has("settings") ? o.getAsJsonObject("settings") : new JsonObject();
                for (Setting s : m.getSettings()) {
                    if (!st.has(s.getName())) continue;
                    if (s instanceof NumberSetting n) n.set(st.get(s.getName()).getAsDouble());
                    else if (s instanceof BooleanSetting b) b.set(st.get(s.getName()).getAsBoolean());
                }
                if (o.has("enabled")) {
                    try { m.setEnabled(o.get("enabled").getAsBoolean()); } catch (Exception ignored) {}
                }
            }
            current = name;
        } catch (Exception ignored) {}
    }

    public static void create() {
        int i = 1;
        while (Files.exists(file("Profile " + i))) i++;
        String n = "Profile " + i;
        save(n);
        current = n;
        refresh();
    }

    public static void delete(String name) {
        if (name.equals("Default")) return;
        try { Files.deleteIfExists(file(name)); } catch (IOException ignored) {}
        if (current.equals(name)) current = "Default";
        refresh();
    }
}
