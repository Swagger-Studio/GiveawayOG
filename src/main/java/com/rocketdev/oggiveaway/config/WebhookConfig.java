package com.rocketdev.oggiveaway.config;



import com.rocketdev.oggiveaway.OGGiveaway;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class WebhookConfig {

    private static File file;
    private static FileConfiguration config;

    public static void setup() {
        file = new File(OGGiveaway.getInstance().getDataFolder(), "webhooks.yml");

        if (!file.exists()) {
            try {
                OGGiveaway.getInstance().saveResource("webhooks.yml", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return config;
    }

    public static void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}