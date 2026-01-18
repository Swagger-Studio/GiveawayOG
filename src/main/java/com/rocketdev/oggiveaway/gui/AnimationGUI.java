package com.rocketdev.oggiveaway.gui;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.manager.AnimationSettingsManager;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class AnimationGUI implements InventoryHolder {
    private final OGGiveaway plugin;
    private final Inventory inv;

    public AnimationGUI(OGGiveaway plugin) {
        this.plugin = plugin;
        this.inv = Bukkit.createInventory(this, 27, ColorUtil.colorize("&8&l✨ Animation Settings"));
        refresh();
    }

    public void refresh() {
        AnimationSettingsManager settings = plugin.getAnimationSettingsManager();


        boolean bsEnabled = settings.isEnabled("BLACKSMITH");
        int bsChance = settings.getChance("BLACKSMITH");

        inv.setItem(11, createItem(Material.ANVIL, "&6&l⚒ Blacksmith Animation",
                "&7Status: " + (bsEnabled ? "&a&lENABLED" : "&c&lDISABLED"),
                "&7Chance: &e" + bsChance + "%",
                "",
                "&eLeft-Click: &fToggle On/Off",
                "&eRight-Click: &fManage Prizes",
                "&bShift-Click: &fSet % via Chat"
        ));


        boolean spEnabled = settings.isEnabled("SPIRAL");
        int spChance = settings.getChance("SPIRAL");

        inv.setItem(15, createItem(Material.FIREWORK_ROCKET, "&b&l🌀 Spiral Animation",
                "&7Status: " + (spEnabled ? "&a&lENABLED" : "&c&lDISABLED"),
                "&7Chance: &e" + spChance + "%",
                "",
                "&eLeft-Click: &fToggle On/Off",
                "&eRight-Click: &fManage Prizes",
                "&bShift-Click: &fSet % via Chat"
        ));


        inv.setItem(22, createItem(Material.ARROW, "&cGo Back"));
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(name));
            meta.setLore(Arrays.stream(lore).map(ColorUtil::colorize).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override public Inventory getInventory() { return inv; }
}