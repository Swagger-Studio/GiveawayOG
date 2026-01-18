package com.rocketdev.oggiveaway.hook;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.manager.GiveawayManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class GiveawayExpansion extends PlaceholderExpansion {

    private final OGGiveaway plugin;

    public GiveawayExpansion(OGGiveaway plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "giveawayog"; // The prefix (e.g. %giveawayog_timer%)
    }

    @Override
    public @NotNull String getAuthor() {
        return "Swagger Studio";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // Placeholder: %giveawayog_timer%
        if (params.equalsIgnoreCase("timer") || params.equalsIgnoreCase("time")) {


            if (GiveawayManager.isRunning) {
                return "§aRunning Now!";
            }


            long nextTime = plugin.getScheduleManager().getNextGiveawayTime();
            if (nextTime <= 0) {
                return "§cPaused";
            }


            long timeLeft = (nextTime - System.currentTimeMillis()) / 1000;
            if (timeLeft <= 0) return "§aStarting...";

            return formatTime(timeLeft);
        }

        // Placeholder: %giveawayog_last_winner%
        if (params.equalsIgnoreCase("winner") || params.equalsIgnoreCase("last_winner")) {
            String winner = plugin.getBossBarManager().getLastWinner();
            return (winner == null) ? "None" : winner;
        }

        return null;
    }

 
    private String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}