package com.rocketdev.oggiveaway.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class LoggerUtil {

    private static final ConsoleCommandSender console = Bukkit.getConsoleSender();
    private static final String PREFIX = "&8[&bGiveawayOG&8] &r";

    // 1. Console Logging (Keep this for errors/startup)
    public static void log(String message) {
        console.sendMessage(ColorUtil.colorize(PREFIX + message));
    }

    public static void warn(String message) {
        console.sendMessage(ColorUtil.colorize(PREFIX + "&eWARN: " + message));
    }

    public static void error(String message) {
        console.sendMessage(ColorUtil.colorize(PREFIX + "&cERROR: " + message));
    }

    // 2. Broadcast to PLAYERS ONLY (No Console)
    public static void broadcast(String message) {
        String colorized = ColorUtil.colorize(message);
        // Loop through players so it DOES NOT log to console
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(colorized);
        }
    }

    // 3. Broadcast a "Block" to PLAYERS ONLY (No Console)
    public static void broadcastBlock(String... messages) {
        String border = ColorUtil.colorize("&8&m-------------------------------------------");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(border);
            for (String line : messages) {
                p.sendMessage(ColorUtil.colorize(line));
            }
            p.sendMessage(border);
        }
    }
}

