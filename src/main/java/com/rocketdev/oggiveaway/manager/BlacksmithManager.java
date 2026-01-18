package com.rocketdev.oggiveaway.manager;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.animation.AnimationFactory;
import com.rocketdev.oggiveaway.animation.impl.LegacyBlacksmithTask;
import com.rocketdev.oggiveaway.animation.impl.ModernBlacksmithTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlacksmithManager implements Listener {

    private final OGGiveaway plugin;
    // Maps Player UUID -> The 'forceEnd' method of their task
    private final Map<UUID, Runnable> activeSessions = new HashMap<>();

    public BlacksmithManager(OGGiveaway plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void startForgeEvent(Player player, List<ItemStack> prizePool) {
        cleanup(player.getUniqueId());

        if (AnimationFactory.isModern()) {
            ModernBlacksmithTask task = new ModernBlacksmithTask(plugin, player, prizePool, this);
            task.runTaskTimer(plugin, 0L, 1L);
            activeSessions.put(player.getUniqueId(), task::forceEnd);
        } else {
            LegacyBlacksmithTask task = new LegacyBlacksmithTask(plugin, player, prizePool, this);
            task.runTaskTimer(plugin, 0L, 1L);
            activeSessions.put(player.getUniqueId(), task::forceEnd);
        }
    }

    // --- FIX: INFINITE LOOP PREVENTION ---
    public void cleanup(UUID uuid) {
        // 1. Remove from Map FIRST
        Runnable stopTask = activeSessions.remove(uuid);

        // 2. Only run the stop logic if it existed
        if (stopTask != null) {
            stopTask.run(); // This calls task.forceEnd()
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cleanup(e.getPlayer().getUniqueId());
    }
}