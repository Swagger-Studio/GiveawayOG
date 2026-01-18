package com.rocketdev.oggiveaway.manager;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PrizeManager {
    private final OGGiveaway plugin;
    private final File file;
    private FileConfiguration config;

    public final NamespacedKey cmdKey;
    public final NamespacedKey durKey;
    public final NamespacedKey uuidKey;

    public PrizeManager(OGGiveaway plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "prizes.yml");

        this.cmdKey = new NamespacedKey(plugin, "cmd");
        this.durKey = new NamespacedKey(plugin, "voucher_duration");
        this.uuidKey = new NamespacedKey(plugin, "uuid");

        loadPrizes();
    }

    public void loadPrizes() {
        if (!file.exists()) plugin.saveResource("prizes.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public List<ItemStack> getPrizesForAnimation(String animationType) {
        String pool = animationType.toLowerCase(); // Force lowercase
        List<ItemStack> combined = new ArrayList<>();

        if (config.contains("pools." + pool + ".items")) {
            List<Map<?, ?>> itemList = config.getMapList("pools." + pool + ".items");
            for (Map<?, ?> map : itemList) combined.add(restoreItem(map));
        }

        List<String> commands = config.getStringList("pools." + pool + ".commands");
        for (String cmd : commands) combined.add(createVoucher(cmd));

        return combined;
    }

    public void addCommandPrize(String pool, String command) {
        List<String> list = config.getStringList("pools." + pool.toLowerCase() + ".commands");
        list.add(command);
        config.set("pools." + pool.toLowerCase() + ".commands", list);
        saveFile();
    }

    public void savePrizePool(String pool, Inventory inv) {
        String poolKey = pool.toLowerCase();
        List<Map<String, Object>> cleanItems = new ArrayList<>();
        List<String> commandStrings = new ArrayList<>();

        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            if (item.getType() == Material.LIME_DYE || item.getType() == Material.RED_DYE) continue;

            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(cmdKey, PersistentDataType.STRING)) {
                commandStrings.add(meta.getPersistentDataContainer().get(cmdKey, PersistentDataType.STRING));
            } else {
                cleanItems.add(cleanItem(item));
            }
        }
        config.set("pools." + poolKey + ".items", cleanItems);
        config.set("pools." + poolKey + ".commands", commandStrings);
        saveFile();
    }


    private ItemStack createVoucher(String command) {
        int durationMin = plugin.getConfig().getInt("settings.voucher-expiry-minutes", 10);
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize("&d&l🎁 Unrevealed Reward"));
            meta.setLore(Arrays.asList(
                    ColorUtil.colorize("&7Right-click to reveal!"),
                    ColorUtil.colorize("&7Duration: &f" + durationMin + "m")
            ));
            meta.getPersistentDataContainer().set(cmdKey, PersistentDataType.STRING, command);
            meta.getPersistentDataContainer().set(durKey, PersistentDataType.INTEGER, durationMin);
            paper.setItemMeta(meta);
        }
        return paper;
    }

    private Map<String, Object> cleanItem(ItemStack item) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("material", item.getType().name());
        data.put("amount", item.getAmount());
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) data.put("name", meta.getDisplayName().replace("§", "&"));
            if (meta.hasLore()) data.put("lore", meta.getLore());
            if (meta.hasEnchants()) {
                Map<String, Integer> enchants = new HashMap<>();
                for (Map.Entry<Enchantment, Integer> e : meta.getEnchants().entrySet())
                    enchants.put(e.getKey().getKey().getKey(), e.getValue());
                data.put("enchantments", enchants);
            }
        }
        return data;
    }

    private ItemStack restoreItem(Map<?, ?> data) {
        try {
            ItemStack item = new ItemStack(Material.valueOf((String)data.get("material")), (Integer)data.get("amount"));
            ItemMeta meta = item.getItemMeta();
            if (data.containsKey("name")) meta.setDisplayName(ColorUtil.colorize((String)data.get("name")));
            if (data.containsKey("lore")) {
                List<String> l = (List<String>) data.get("lore");
                List<String> cL = new ArrayList<>();
                for(String s : l) cL.add(ColorUtil.colorize(s));
                meta.setLore(cL);
            }
            if (data.containsKey("enchantments")) {
                Map<String, Integer> enchants = (Map<String, Integer>) data.get("enchantments");
                for (Map.Entry<String, Integer> e : enchants.entrySet()) {
                    meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(e.getKey())), e.getValue(), true);
                }
            }
            item.setItemMeta(meta);
            return item;
        } catch (Exception e) { return new ItemStack(Material.STONE); }
    }

    private void saveFile() {
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}