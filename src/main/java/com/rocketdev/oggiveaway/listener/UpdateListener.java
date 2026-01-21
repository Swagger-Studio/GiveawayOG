package com.rocketdev.oggiveaway.listener;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import com.rocketdev.oggiveaway.utils.UpdateChecker;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener implements Listener {

    private final OGGiveaway plugin;
    private final int resourceId = 131882;

    public UpdateListener(OGGiveaway plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("giveawayog.admin")) return;

        new UpdateChecker(plugin, resourceId).getVersion(version -> {
            if (!plugin.getDescription().getVersion().equalsIgnoreCase(version)) {
                event.getPlayer().sendMessage(ColorUtil.colorize("&8&m----------------------------------"));
                event.getPlayer().sendMessage(ColorUtil.colorize("&b&lOGGiveaway &7» &fA new update is available!"));
                event.getPlayer().sendMessage(ColorUtil.colorize("&7Current: &c" + plugin.getDescription().getVersion() + " &7New: &a" + version));

                TextComponent message = new TextComponent(ColorUtil.colorize("&a&l[CLICK TO UPDATE]"));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/" + resourceId));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to go to SpigotMC page").create()));

                event.getPlayer().spigot().sendMessage(message);
                event.getPlayer().sendMessage(ColorUtil.colorize("&8&m----------------------------------"));
            }
        });
    }
}