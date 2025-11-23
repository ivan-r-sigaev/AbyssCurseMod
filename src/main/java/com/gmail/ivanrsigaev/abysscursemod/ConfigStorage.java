package com.gmail.ivanrsigaev.abysscursemod;

import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraftforge.fml.loading.FMLPaths;

public class ConfigStorage {
    public static final String CONFIG_FILENAME = "abyss_curse_config.json";

    public static Config getConfig() {
        return config;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = new String(Files.readAllBytes(CONFIG_PATH));
                config = Config.fromJson(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, config.toJson());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILENAME);
    private static Config config = Config.defaultValues();
}
