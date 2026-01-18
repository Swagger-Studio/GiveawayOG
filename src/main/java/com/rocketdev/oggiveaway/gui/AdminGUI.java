package com.rocketdev.oggiveaway.gui;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.manager.GiveawayManager;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminGUI {

    public static void openDashboard(Player player, OGGiveaway plugin) {
        Inventory inv = Bukkit.createInventory(null, 45, ColorUtil.colorize("&8⚡ GiveawayOG Settings"));


        updateClockItem(inv, plugin);


        inv.setItem(20, createItem(Material.COMPARATOR, "&6&l⚙ ANIMATION SETTINGS",
                "&7Toggle animations & chances."));

        if (GiveawayManager.isRunning) {
            inv.setItem(22, createItem(Material.REDSTONE_BLOCK, "&c&l⛔ FORCE STOP",
                    "&7Click to &cIMMEDIATELY", "&7stop the giveaway."));
        } else {
            inv.setItem(22, createItem(Material.EMERALD_BLOCK, "&a&l🚀 START GIVEAWAY",
                    "&7Click to force start", "&7right now!"));
        }

        inv.setItem(24, createItem(Material.CHEST, "&b&l💎 MANAGE PRIZES",
                "&7Edit prize pools."));


        int minPlayers = plugin.getConfig().getInt("settings.min-players", 1);
        inv.setItem(30, createMinPlayerItem(minPlayers));

        inv.setItem(31, createItem(Material.PAPER, "&d&l🎁 CREATE VOUCHER",
                "&7Get voucher command."));

        inv.setItem(32, createItem(Material.NETHER_STAR, "&e&l🔄 RELOAD",
                "&7Reload Config."));

        player.openInventory(inv);

        new BukkitRunnable() {
            @Override
            public void run() {

                if (player.getOpenInventory().getTopInventory() == null
                        || !player.getOpenInventory().getTitle().contains("GiveawayOG Settings")) {
                    this.cancel();
                    return;
                }


                updateClockItem(player.getOpenInventory().getTopInventory(), plugin);
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }


    private static void updateClockItem(Inventory inv, OGGiveaway plugin) {
        String timeRemaining = plugin.getScheduleManager().getTimeRemaining();
        long intervalSeconds = plugin.getConfig().getLong("scheduler.interval-seconds", 0);
        long minutes = intervalSeconds / 60;
        boolean isSchedulerOn = intervalSeconds > 0;

        ItemStack clock = createItem(Material.CLOCK, "&e&l⏰ SCHEDULER: " + (isSchedulerOn ? "&a&lON" : "&c&lOFF"),
                "&7Current Interval: &f" + minutes + " mins",
                "&7Next Event: &f" + timeRemaining,
                " ",
                "&a[Left-Click] &7+5 Minutes",
                "&c[Right-Click] &7-5 Minutes",
                "&e[Shift-Click] &7Toggle ON/OFF"
        );

        inv.setItem(4, clock);
    }

    public static void openAnimationSettings(Player player, OGGiveaway plugin) {
        Inventory inv = Bukkit.createInventory(null, 36, ColorUtil.colorize("&8⚙ &0&lAnimation Config"));

        boolean bsEnabled = plugin.getConfig().getBoolean("animations.blacksmith.enabled");
        int bsChance = plugin.getConfig().getInt("animations.blacksmith.chance");
        inv.setItem(11, createToggleItem(Material.ANVIL, "&f&lBLACKSMITH", bsEnabled));
        inv.setItem(12, createChanceItem(plugin, bsChance, "animations.blacksmith.chance"));

        boolean spEnabled = plugin.getConfig().getBoolean("animations.spiral.enabled");
        int spChance = plugin.getConfig().getInt("animations.spiral.chance");
        inv.setItem(20, createToggleItem(Material.BEACON, "&f&lSPIRAL", spEnabled));
        inv.setItem(21, createChanceItem(plugin, spChance, "animations.spiral.chance"));

        inv.setItem(31, createItem(Material.ARROW, "&c&l⬅ BACK", "&7Return to Dashboard"));
        player.openInventory(inv);
    }

    public static void openPoolSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ColorUtil.colorize("&8💎 &0&lSelect Pool"));
        inv.setItem(11, createItem(Material.ANVIL, "&e&lBLACKSMITH POOL"));
        inv.setItem(13, createItem(Material.BEACON, "&b&lSPIRAL POOL"));
        inv.setItem(26, createItem(Material.ARROW, "&c&l⬅ BACK"));
        player.openInventory(inv);
    }

    public static void openPrizeEditor(Player player, OGGiveaway plugin, String poolName) {
        String title = ColorUtil.colorize("&8💎 Editing: &n" + poolName);
        Inventory inv = Bukkit.createInventory(null, 54, title);

        List<ItemStack> prizes = plugin.getPrizeManager().getPrizesForAnimation(poolName);
        for (ItemStack p : prizes) if (p != null) inv.addItem(p);

        inv.setItem(53, createItem(Material.LIME_DYE, "&a&l✔ SAVE"));
        inv.setItem(45, createItem(Material.RED_DYE, "&c&l✖ CANCEL"));
        player.openInventory(inv);
    }


    private static ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.colorize(name));
        List<String> coloredLore = new ArrayList<>();
        for (String s : lore) coloredLore.add(ColorUtil.colorize(s));
        meta.setLore(coloredLore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createMinPlayerItem(int count) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.colorize("&9&l👥 MIN PLAYERS: &f" + count));
        meta.setLore(Arrays.asList(
                ColorUtil.colorize("&7Required players to start."),
                " ",
                ColorUtil.colorize("&a[Left-Click] &7+1"),
                ColorUtil.colorize("&c[Right-Click] &7-1")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createToggleItem(Material mat, String name, boolean enabled) {
        return createItem(mat, name, "&7Status: " + (enabled ? "&a✔ ON" : "&c✖ OFF"), "&7Click to toggle.");
    }

    private static ItemStack createChanceItem(OGGiveaway plugin, int chance, String path) {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ColorUtil.colorize("&e&l⚡ CHANCE: &f" + chance + "%"));
        meta.setLore(Arrays.asList(
                ColorUtil.colorize("&a[Left] &7+10%"),
                ColorUtil.colorize("&c[Right] &7-10%")
        ));

        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(plugin, "config_path");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, path);
        item.setItemMeta(meta);
        return item;
    }
}