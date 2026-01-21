package com.rocketdev.oggiveaway.manager;

import com.rocketdev.oggiveaway.OGGiveaway;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Random;

public class AnimationSettingsManager {
    private final OGGiveaway plugin;
    private final Random random = new Random();

    public AnimationSettingsManager(OGGiveaway plugin) {
        this.plugin = plugin;
        loadDefaults();
    }

    public void loadDefaults() {
        FileConfiguration config = plugin.getConfig();
        config.addDefault("animations.blacksmith.enabled", true);
        config.addDefault("animations.blacksmith.chance", 50);
        config.addDefault("animations.spiral.enabled", true);
        config.addDefault("animations.spiral.chance", 50);
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public boolean isEnabled(String type) {
        return plugin.getConfig().getBoolean("animations." + type.toLowerCase() + ".enabled", true);
    }

    public int getChance(String type) {
        return plugin.getConfig().getInt("animations." + type.toLowerCase() + ".chance", 50);
    }

    public String pickRandomAnimation() {
        boolean blacksmithOn = isEnabled("BLACKSMITH");
        boolean spiralOn = isEnabled("SPIRAL");

        if (!blacksmithOn && !spiralOn) {
            return "BLACKSMITH";
        }

        if (!blacksmithOn) return "SPIRAL";
        if (!spiralOn) return "BLACKSMITH";

        int roll = random.nextInt(100) + 1;
        int blacksmithThreshold = getChance("BLACKSMITH");

        return (roll <= blacksmithThreshold) ? "BLACKSMITH" : "SPIRAL";
    }
}