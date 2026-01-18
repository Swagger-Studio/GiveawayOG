

// Right now removed





package com.rocketdev.oggiveaway.listener;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.gui.AnimationGUI;
import com.rocketdev.oggiveaway.gui.PrizeGUI;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputListener implements Listener {
    private final OGGiveaway plugin;


    public static final Map<UUID, String> awaitingInput = new HashMap<>();

    public ChatInputListener(OGGiveaway plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!awaitingInput.containsKey(p.getUniqueId())) return;

        e.setCancelled(true);
        String input = e.getMessage().trim();
        String mode = awaitingInput.remove(p.getUniqueId());


        if (mode.startsWith("CMD_")) {
            String poolType = mode.replace("CMD_", "");



            new BukkitRunnable() {
                @Override public void run() {

                    plugin.getPrizeManager().addCommandPrize(poolType, input);

                    p.sendMessage(ColorUtil.colorize("&a✅ Added Command Voucher: &f" + input));
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

                    p.openInventory(new PrizeGUI(plugin, poolType).getInventory());
                }
            }.runTask(plugin);
        }


        else if (mode.startsWith("CHANCE_")) {
            String animType = mode.replace("CHANCE_", "");
            try {
                int chance = Integer.parseInt(input);
                if (chance < 0 || chance > 100) {
                    p.sendMessage(ColorUtil.colorize("&cPlease enter 0-100."));
                    return;
                }

                plugin.getAnimationSettingsManager().setChance(animType, chance);
                p.sendMessage(ColorUtil.colorize("&a✅ " + animType + " Chance set to " + chance + "%"));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

                new BukkitRunnable() {
                    @Override public void run() {
                        p.openInventory(new AnimationGUI(plugin).getInventory());
                    }
                }.runTask(plugin);

            } catch (NumberFormatException ex) {
                p.sendMessage(ColorUtil.colorize("&cInvalid number. Cancelled."));
            }
        }
    }
}