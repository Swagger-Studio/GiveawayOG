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

    
    public void cleanup(UUID uuid) {
        
        Runnable stopTask = activeSessions.remove(uuid);

        
        if (stopTask != null) {
            stopTask.run(); 
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cleanup(e.getPlayer().getUniqueId());
    }
}
