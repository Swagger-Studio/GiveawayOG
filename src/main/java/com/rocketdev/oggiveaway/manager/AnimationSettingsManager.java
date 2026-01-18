package com.rocketdev.oggiveaway.manager;

import com.rocketdev.oggiveaway.OGGiveaway;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AnimationSettingsManager {
    private final OGGiveaway plugin;
    private final Map<String, Boolean> status = new HashMap<>();
    private final Map<String, Integer> chances = new HashMap<>();
    private final Random random = new Random();

    public AnimationSettingsManager(OGGiveaway plugin) {
        this.plugin = plugin;
        loadSettings();
    }

    public void loadSettings() {
        FileConfiguration config = plugin.getConfig();

        config.addDefault("animations.blacksmith.enabled", true);
        config.addDefault("animations.blacksmith.chance", 50);
        config.addDefault("animations.spiral.enabled", true);
        config.addDefault("animations.spiral.chance", 50);
        plugin.saveConfig();

        status.put("BLACKSMITH", config.getBoolean("animations.blacksmith.enabled"));
        chances.put("BLACKSMITH", config.getInt("animations.blacksmith.chance"));

        status.put("SPIRAL", config.getBoolean("animations.spiral.enabled"));
        chances.put("SPIRAL", config.getInt("animations.spiral.chance"));
    }

    public boolean isEnabled(String type) { return status.getOrDefault(type, true); }
    public int getChance(String type) { return chances.getOrDefault(type, 50); }

    public void setStatus(String type, boolean enabled) {

        if (!enabled && getEnabledCount() <= 1 && isEnabled(type)) return;

        status.put(type, enabled);
        plugin.getConfig().set("animations." + type.toLowerCase() + ".enabled", enabled);
        plugin.saveConfig();
    }

    public void setChance(String type, int chance) {
        chance = Math.max(0, Math.min(100, chance));
        chances.put(type, chance);
        plugin.getConfig().set("animations." + type.toLowerCase() + ".chance", chance);
        plugin.saveConfig();
    }

    private int getEnabledCount() {
        return (int) status.values().stream().filter(b -> b).count();
    }


    public String pickRandomAnimation() {

        if (!isEnabled("BLACKSMITH")) return "SPIRAL";

        if (!isEnabled("SPIRAL")) return "BLACKSMITH";


        int roll = random.nextInt(100) + 1;
        int blacksmithThreshold = getChance("BLACKSMITH");


        return (roll <= blacksmithThreshold) ? "BLACKSMITH" : "SPIRAL";
    }
}