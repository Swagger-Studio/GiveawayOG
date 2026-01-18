package com.rocketdev.oggiveaway.listener;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class VoucherListener implements Listener {

    private final OGGiveaway plugin;
    private final NamespacedKey cmdKey;
    private final NamespacedKey expKey;

    public VoucherListener(OGGiveaway plugin) {
        this.plugin = plugin;
        this.cmdKey = new NamespacedKey(plugin, "cmd");
        this.expKey = new NamespacedKey(plugin, "expiry");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        Player p = e.getPlayer();


        if (meta.getPersistentDataContainer().has(cmdKey, PersistentDataType.STRING)) {
            e.setCancelled(true);


            if (meta.getPersistentDataContainer().has(expKey, PersistentDataType.LONG)) {
                long expiry = meta.getPersistentDataContainer().get(expKey, PersistentDataType.LONG);
                if (System.currentTimeMillis() > expiry) {
                    p.sendMessage(ColorUtil.colorize("&c⚠ This voucher has expired!"));
                    p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                    item.setAmount(0); // Delete expired item
                    return;
                }
            }


            String command = meta.getPersistentDataContainer().get(cmdKey, PersistentDataType.STRING);
            if (command != null && !command.isEmpty()) {




                if (command.startsWith("/")) {
                    command = command.substring(1);
                }


                if (command.startsWith("give ")) {
                    command = "minecraft:" + command;
                }


                String finalCmd = command.replace("%player%", p.getName());


                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);


                p.sendMessage(ColorUtil.colorize("&a&l🎁 Reward Redeemed!"));
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);


                item.setAmount(item.getAmount() - 1);
            }
        }
    }
}