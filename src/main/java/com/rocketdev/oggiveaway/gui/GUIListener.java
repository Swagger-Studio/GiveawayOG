package com.rocketdev.oggiveaway.gui;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GUIListener implements Listener {

    private final OGGiveaway plugin;
    private final NamespacedKey pathKey;

    public GUIListener(OGGiveaway plugin) {
        this.plugin = plugin;
        this.pathKey = new NamespacedKey(plugin, "config_path");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        String title = ChatColor.stripColor(e.getView().getTitle());
        Player p = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();

        if (title.contains("GiveawayOG Settings")) {
            e.setCancelled(true);
            if (clicked == null || clicked.getType() == Material.AIR) return;

            Material mat = clicked.getType();

            if (mat == Material.CLOCK) {
                long currentSeconds = plugin.getConfig().getLong("scheduler.interval-seconds", 0);

                if (e.getClick().isShiftClick()) {
                    if (currentSeconds > 0) {
                        currentSeconds = 0;
                        p.sendMessage(ColorUtil.colorize("&c&lScheduler Disabled!"));
                    } else {
                        currentSeconds = 3600;
                        p.sendMessage(ColorUtil.colorize("&a&lScheduler Enabled (60m)!"));
                    }
                } else if (e.getClick().isLeftClick()) {
                    currentSeconds += 300;
                } else if (e.getClick().isRightClick()) {
                    currentSeconds -= 300;
                }

                if (currentSeconds < 0) currentSeconds = 0;

                plugin.getScheduleManager().setInterval(currentSeconds);
                playSound(p);
                AdminGUI.openDashboard(p, plugin);

            } else if (mat == Material.EMERALD_BLOCK) {
                p.closeInventory();
                plugin.getGiveawayManager().startGiveaway(null);

            } else if (mat == Material.REDSTONE_BLOCK) {
                p.closeInventory();
                plugin.getGiveawayManager().cancelGiveaway();

            } else if (mat == Material.COMPARATOR) {
                AdminGUI.openAnimationSettings(p, plugin);
                playSound(p);

            } else if (mat == Material.CHEST) {
                AdminGUI.openPoolSelector(p);
                playSound(p);

            } else if (mat == Material.PAPER) {
                p.closeInventory();
                p.sendMessage(ColorUtil.colorize("&d&lINFO: &fUse /gw createvoucher <command>"));

            } else if (mat == Material.NETHER_STAR) {
                plugin.getConfigManager().reload();
                plugin.getPrizeManager().loadPrizes();
                p.sendMessage(ColorUtil.colorize("&a&l✔ Reloaded!"));
                playSound(p);
                AdminGUI.openDashboard(p, plugin);

            } else if (mat == Material.PLAYER_HEAD) {
                int current = plugin.getConfig().getInt("settings.min-players", 1);
                if (e.getClick().isLeftClick()) current++;
                if (e.getClick().isRightClick()) current--;
                if (current < 1) current = 1;

                plugin.getConfig().set("settings.min-players", current);
                plugin.saveConfig();
                playSound(p);
                AdminGUI.openDashboard(p, plugin);
            }
        }
        else if (title.contains("Animation Config")) {
            e.setCancelled(true);
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ARROW) {
                AdminGUI.openDashboard(p, plugin);
                return;
            }

            if (clicked.getType() == Material.ANVIL) {
                toggleSetting(p, "animations.blacksmith.enabled");
            } else if (clicked.getType() == Material.BEACON) {
                toggleSetting(p, "animations.spiral.enabled");
            } else if (clicked.getType() == Material.GOLD_NUGGET) {
                ItemMeta meta = clicked.getItemMeta();
                if (meta == null) return;

                String path = meta.getPersistentDataContainer().get(pathKey, PersistentDataType.STRING);
                if (path == null) return;

                int currentChance = plugin.getConfig().getInt(path);
                int newChance = currentChance;

                if (e.getClick().isLeftClick()) newChance += 10;
                if (e.getClick().isRightClick()) newChance -= 10;

                if (newChance < 0) newChance = 0;
                if (newChance > 100) newChance = 100;

                plugin.getConfig().set(path, newChance);
                plugin.saveConfig();
                playSound(p);
                AdminGUI.openAnimationSettings(p, plugin);
            }
        }
        else if (title.contains("Select Pool")) {
            e.setCancelled(true);
            if (clicked == null) return;
            if (clicked.getType() == Material.ARROW) AdminGUI.openDashboard(p, plugin);
            else if (clicked.getType() == Material.ANVIL) AdminGUI.openPrizeEditor(p, plugin, "blacksmith");
            else if (clicked.getType() == Material.BEACON) AdminGUI.openPrizeEditor(p, plugin, "spiral");
        } else if (title.contains("Editing:")) {
            boolean isTopInv = e.getClickedInventory().equals(e.getView().getTopInventory());
            if (isTopInv && clicked != null) {
                if (clicked.getType() == Material.LIME_DYE && e.getSlot() == 53) {
                    e.setCancelled(true);
                    String[] parts = title.split(": ");
                    if (parts.length > 1) {
                        plugin.getPrizeManager().savePrizePool(parts[1].trim(), e.getView().getTopInventory());
                        p.sendMessage(ColorUtil.colorize("&a&l✔ Saved!"));
                        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                    }
                    p.closeInventory();
                    return;
                }
                if (clicked.getType() == Material.RED_DYE && e.getSlot() == 45) {
                    e.setCancelled(true);
                    AdminGUI.openPoolSelector(p);
                    return;
                }
            }
        }
    }

    private void playSound(Player p) {
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    private void toggleSetting(Player p, String path) {
        boolean current = plugin.getConfig().getBoolean(path);

        if (current) {
            int enabledCount = 0;
            if (plugin.getConfig().getBoolean("animations.blacksmith.enabled")) enabledCount++;
            if (plugin.getConfig().getBoolean("animations.spiral.enabled")) enabledCount++;

            if (enabledCount <= 1) {
                p.sendMessage(ColorUtil.colorize("&c⚠ You must have at least one animation enabled!"));
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
        }

        plugin.getConfig().set(path, !current);
        plugin.saveConfig();
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
        AdminGUI.openAnimationSettings(p, plugin);
    }
}
