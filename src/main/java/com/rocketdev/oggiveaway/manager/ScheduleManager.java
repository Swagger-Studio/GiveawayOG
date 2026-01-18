package com.rocketdev.oggiveaway.manager;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.LoggerUtil;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;

public class ScheduleManager {

    private final OGGiveaway plugin;
    private BukkitTask task;
    private Instant nextIntervalRun;
    private long intervalSeconds = 0;
    private long lastAnnouncedSecond = -1;

    public ScheduleManager(OGGiveaway plugin) {
        this.plugin = plugin;
        loadInterval();
        startScheduler();
    }

    private void loadInterval() {
        this.intervalSeconds = plugin.getConfig().getLong("scheduler.interval-seconds", 0);
        if (intervalSeconds > 0) {
            this.nextIntervalRun = Instant.now().plusSeconds(intervalSeconds);
        }
    }

    public void startScheduler() {
        stopScheduler();

        this.intervalSeconds = plugin.getConfig().getLong("scheduler.interval-seconds", 0);
        if (intervalSeconds <= 0) return;

        if (nextIntervalRun == null || nextIntervalRun.isBefore(Instant.now())) {
            this.nextIntervalRun = Instant.now().plusSeconds(intervalSeconds);
        }

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (intervalSeconds <= 0 || nextIntervalRun == null) {
                    cancel();
                    return;
                }

                if (GiveawayManager.isRunning) {
                    nextIntervalRun = Instant.now().plusSeconds(intervalSeconds);
                    return;
                }

                long secondsLeft = Duration.between(Instant.now(), nextIntervalRun).getSeconds();

                if (secondsLeft != lastAnnouncedSecond) {
                    if (secondsLeft == 10) {
                        LoggerUtil.broadcast("&eGiveaway starting in &c10 &eseconds!");
                    } else if (secondsLeft == 5) {
                        LoggerUtil.broadcast("&eGiveaway starting in &c5 &eseconds!");
                    } else if (secondsLeft <= 3 && secondsLeft > 0) {
                        LoggerUtil.broadcast("&eGiveaway starting in &c" + secondsLeft + "...");
                    }
                    lastAnnouncedSecond = secondsLeft;
                }

                if (Instant.now().isAfter(nextIntervalRun)) {
                    plugin.getGiveawayManager().startGiveaway(null);
                    nextIntervalRun = Instant.now().plusSeconds(intervalSeconds);
                    lastAnnouncedSecond = -1;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopScheduler() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        task = null;
    }

    public void setInterval(long seconds) {
        this.intervalSeconds = seconds;
        plugin.getConfig().set("scheduler.interval-seconds", seconds);
        plugin.saveConfig();

        if (seconds > 0) {
            this.nextIntervalRun = Instant.now().plusSeconds(seconds);
            startScheduler();
        } else {
            this.nextIntervalRun = null;
            stopScheduler();
        }
    }

    public String getTimeRemaining() {
        if (intervalSeconds > 0 && nextIntervalRun != null) {
            long secondsLeft = Duration.between(Instant.now(), nextIntervalRun).getSeconds();
            if (secondsLeft < 0) secondsLeft = 0;
            long hours = secondsLeft / 3600;
            long minutes = (secondsLeft % 3600) / 60;
            long seconds = secondsLeft % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return "Disabled";
    }

    public long getNextGiveawayTime() {
        if (nextIntervalRun == null || intervalSeconds <= 0) return -1;
        return nextIntervalRun.toEpochMilli();
    }
}