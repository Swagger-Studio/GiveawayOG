package com.rocketdev.oggiveaway.listener;

import com.rocketdev.oggiveaway.OGGiveaway;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private final OGGiveaway plugin;

    public ConnectionListener(OGGiveaway plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGiveawayManager().handleDisconnect(event.getPlayer());

        if (plugin.getBossBarManager() != null) {
            plugin.getBossBarManager().removePlayer(event.getPlayer());
        }
    }
}