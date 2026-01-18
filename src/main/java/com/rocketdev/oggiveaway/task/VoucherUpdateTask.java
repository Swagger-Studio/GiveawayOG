package com.rocketdev.oggiveaway.task;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class VoucherUpdateTask extends BukkitRunnable {

    private final OGGiveaway plugin;
    private final NamespacedKey expKey;

    public VoucherUpdateTask(OGGiveaway plugin) {
        this.plugin = plugin;
        this.expKey = new NamespacedKey(plugin, "expiry");
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerInventory inv = p.getInventory();
            ItemStack mainHand = inv.getItemInMainHand();
            ItemStack offHand = inv.getItemInOffHand();

            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);

                if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) continue;

                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();

                if (pdc.has(expKey, PersistentDataType.LONG)) {
                    long expiryTime = pdc.get(expKey, PersistentDataType.LONG);
                    long timeLeftMillis = expiryTime - now;

                    if (timeLeftMillis <= 0) {
                        inv.setItem(i, null);
                        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                        p.sendMessage(ColorUtil.colorize("&c❌ An unrevealed reward has expired!"));
                        continue;
                    }

                    long secondsTotal = timeLeftMillis / 1000;
                    String timeString = String.format("%02d:%02d", secondsTotal / 60, secondsTotal % 60);

                    List<String> lore = meta.getLore();
                    if (lore != null && lore.size() >= 2) {
                        String newLoreLine = ColorUtil.colorize("&7Expires in: &e" + timeString);
                        if (!lore.get(1).equals(newLoreLine)) {
                            lore.set(1, newLoreLine);
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        }
                    }

                    boolean isHolding = item.equals(mainHand) || item.equals(offHand);
                    if (isHolding) {
                        String msg = ColorUtil.colorize("&6⏳ Voucher Expires in: &e" + timeString);
                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
                    }
                }
            }
        }
    }
}