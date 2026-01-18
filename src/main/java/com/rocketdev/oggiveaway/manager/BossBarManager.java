package com.rocketdev.oggiveaway.manager;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossBarManager {
    private final OGGiveaway plugin;
    private BossBar activeBar;
    private BukkitTask activeTask;
    private final Random random = new Random();
    private final List<String> playerNames = new ArrayList<>();

    private String lastWinner = "None";

    public BossBarManager(OGGiveaway plugin) {
        this.plugin = plugin;
    }

    public void startGiveawayBar() {
        if (activeBar != null) activeBar.removeAll();

        playerNames.clear();
        for (Player p : Bukkit.getOnlinePlayers()) playerNames.add(p.getName());
        if (playerNames.isEmpty()) playerNames.add("Waiting...");

        activeBar = Bukkit.createBossBar(
                ColorUtil.colorize("&f&l⚡ Initializing..."),
                BarColor.WHITE,
                BarStyle.SOLID
        );

        for (Player p : Bukkit.getOnlinePlayers()) activeBar.addPlayer(p);

        runSmoothLoop(0);
    }

    private void runSmoothLoop(int currentTick) {
        if (activeBar == null) return;

        double progress = Math.min(1.0, (double) currentTick / 100.0);
        activeBar.setProgress(progress);

        if (currentTick % 3 == 0) {
            String randomName = playerNames.get(random.nextInt(playerNames.size()));
            String msg = plugin.getConfig().getString("messages.bossbar.rolling", "&b&l⚡ Choosing... &f%player%");
            activeBar.setTitle(ColorUtil.colorize(msg.replace("%player%", randomName)));
        }

        if (currentTick % 5 == 0) {
            activeBar.setColor(activeBar.getColor() == BarColor.BLUE ? BarColor.WHITE : BarColor.BLUE);
        }

        long nextDelay = 1L;

        if (currentTick > 80) nextDelay = 3L;
        if (currentTick > 95) nextDelay = 10L;

        if (currentTick >= 110) return;

        int nextTick = currentTick + 1;
        activeTask = new BukkitRunnable() {
            @Override
            public void run() {
                runSmoothLoop(nextTick);
            }
        }.runTaskLater(plugin, nextDelay);
    }

    public void setWinner(String winnerName) {
        this.lastWinner = winnerName;

        if (activeTask != null && !activeTask.isCancelled()) activeTask.cancel();

        if (activeBar != null) {
            String msg = plugin.getConfig().getString("messages.bossbar.winner", "&b&l💎 WINNER: &f%player% &b&l💎");
            activeBar.setTitle(ColorUtil.colorize(msg.replace("%player%", winnerName)));

            activeBar.setColor(BarColor.BLUE);
            activeBar.setStyle(BarStyle.SEGMENTED_6);
            activeBar.setProgress(1.0);

            new BukkitRunnable() {
                @Override
                public void run() { removeAll(); }
            }.runTaskLater(plugin, 100L);
        }
    }

    public void removePlayer(Player p) {
        if (activeBar != null) {
            activeBar.removePlayer(p);
        }
    }

    public void removeAll() {
        if (activeTask != null && !activeTask.isCancelled()) activeTask.cancel();
        if (activeBar != null) {
            activeBar.removeAll();
            activeBar = null;
        }
    }

    public String getLastWinner() {
        return lastWinner;
    }
}