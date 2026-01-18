package com.rocketdev.oggiveaway.config;

import com.rocketdev.oggiveaway.OGGiveaway;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public class ConfigManager {

    private final OGGiveaway plugin;

    public ConfigManager(OGGiveaway plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();

        config.addDefault("settings.log-to-console", false);
        config.addDefault("settings.min-players", 1);
        config.addDefault("settings.voucher-expiry-minutes", 10);
        config.addDefault("messages.prefix", "&#3399ff&lOGGiveaway &8» &f");
        config.addDefault("scheduler.interval-seconds", 0);
        config.addDefault("animations.blacksmith.enabled", true);
        config.addDefault("animations.blacksmith.chance", 50);
        config.addDefault("animations.spiral.enabled", true);
        config.addDefault("animations.spiral.chance", 50);

        if (!config.contains("messages.broadcast.start")) {
            config.set("messages.broadcast.start", Arrays.asList(
                    "&8&m                                                &r",
                    " &b&l🎉 GIVEAWAY STARTED! 🎉",
                    " ",
                    " &fRolling the dice...",
                    " &bCheck the center of the hub!",
                    "&8&m                                                &r"
            ));
        }

        if (!config.contains("messages.broadcast.winner")) {
            config.set("messages.broadcast.winner", Arrays.asList(
                    "&8&m                                                &r",
                    " &b&l💎 GIVEAWAY ENDED! 💎",
                    " ",
                    " &fThe winner is:",
                    " &b&n%player%",
                    "&8&m                                                &r"
            ));
        }

        config.addDefault("messages.bossbar.rolling", "&b&l⚡ Choosing... &f%player%");
        config.addDefault("messages.bossbar.winner", "&b&l💎 WINNER: &f%player% &b&l💎");

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        loadConfig();
        plugin.getPrizeManager().loadPrizes();
    }
}