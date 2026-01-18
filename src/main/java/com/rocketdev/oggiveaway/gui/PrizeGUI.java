package com.rocketdev.oggiveaway.gui;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class PrizeGUI implements InventoryHolder {
    private final OGGiveaway plugin;
    private final Inventory inv;
    private final String poolType;

    public PrizeGUI(OGGiveaway plugin, String poolType) {
        this.plugin = plugin;
        this.poolType = poolType;
        this.inv = Bukkit.createInventory(this, 36, ColorUtil.colorize("&8Editing: &9" + poolType));
        loadItems();
    }

    private void loadItems() {

        List<ItemStack> items = plugin.getPrizeManager().getPrizesForAnimation(poolType);
        for (ItemStack item : items) {
            if (inv.firstEmpty() != -1) inv.addItem(item);
        }


        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ColorUtil.colorize("&e&lℹ How to use"));
        meta.setLore(Arrays.asList(
                ColorUtil.colorize("&7Drag physical items here."),
                ColorUtil.colorize("&7Use the Command Block button"),
                ColorUtil.colorize("&7to add Voucher Commands.")
        ));
        info.setItemMeta(meta);
        inv.setItem(4, info);


        ItemStack cmdBtn = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta cmdMeta = cmdBtn.getItemMeta();
        cmdMeta.setDisplayName(ColorUtil.colorize("&a&l✚ Add Command Reward"));
        cmdMeta.setLore(Arrays.asList(
                ColorUtil.colorize("&7Click to add a command"),
                ColorUtil.colorize("&7via chat input."),
                ColorUtil.colorize("&7(Creates a Voucher)")
        ));
        cmdBtn.setItemMeta(cmdMeta);
        inv.setItem(8, cmdBtn);
    }

    public String getPoolType() {
        return poolType;
    }

    @Override public Inventory getInventory() { return inv; }
}