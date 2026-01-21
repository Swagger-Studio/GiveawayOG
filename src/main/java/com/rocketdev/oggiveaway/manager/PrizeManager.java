package com.rocketdev.oggiveaway.manager;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
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
import java.util.logging.Level;

public class PrizeManager {
    private final OGGiveaway plugin;
    private final File file;
    private FileConfiguration config;

    public final NamespacedKey cmdKey;
    public final NamespacedKey durKey;
    public final NamespacedKey uuidKey;

    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();

    public PrizeManager(OGGiveaway plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "prizes.yml");

        this.cmdKey = new NamespacedKey(plugin, "cmd");
        this.durKey = new NamespacedKey(plugin, "voucher_duration");
        this.uuidKey = new NamespacedKey(plugin, "uuid");

        loadPrizes();
    }

    public void loadPrizes() {
        if (!file.exists()) {
            createDefaultPrizes();
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void createDefaultPrizes() {
        try {
            if (!file.getParentFile().exists()) {
                boolean ignored = file.getParentFile().mkdirs();
            }
            if (file.createNewFile()) {
                plugin.getLogger().info("Created default prizes.yml");
            }

            config = YamlConfiguration.loadConfiguration(file);

            createBlacksmithDefaults();
            createSpiralDefaults();

            config.save(file);

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create default prizes", e);
        }
    }

    private void createBlacksmithDefaults() {
        List<Map<String, Object>> blacksmithItems = new ArrayList<>();

        Map<String, Object> sword = new LinkedHashMap<>();
        sword.put("material", "DIAMOND_SWORD");
        sword.put("amount", 1);

        Map<String, Integer> swordEnchants = new HashMap<>();
        swordEnchants.put("sharpness", 5);
        sword.put("enchantments", swordEnchants);
        blacksmithItems.add(sword);

        Map<String, Object> gapple = new LinkedHashMap<>();
        gapple.put("material", "GOLDEN_APPLE");
        gapple.put("amount", 5);
        blacksmithItems.add(gapple);

        config.set("pools.blacksmith.items", blacksmithItems);

        List<String> blacksmithCmds = new ArrayList<>();
        blacksmithCmds.add("give %player% iron_ingot 32");
        blacksmithCmds.add("give %player% iron_sword 1");
        blacksmithCmds.add("give %player% shield 1");
        blacksmithCmds.add("give %player% bow 1");
        blacksmithCmds.add("give %player% arrow 64");
        config.set("pools.blacksmith.commands", blacksmithCmds);
    }

    private void createSpiralDefaults() {
        List<Map<String, Object>> spiralItems = new ArrayList<>();

        Map<String, Object> totem = new LinkedHashMap<>();
        totem.put("material", "TOTEM_OF_UNDYING");
        totem.put("amount", 1);
        spiralItems.add(totem);

        Map<String, Object> netherite = new LinkedHashMap<>();
        netherite.put("material", "NETHERITE_INGOT");
        netherite.put("amount", 1);
        spiralItems.add(netherite);

        config.set("pools.spiral.items", spiralItems);

        List<String> spiralCmds = new ArrayList<>();
        spiralCmds.add("give %player% emerald 16");
        spiralCmds.add("give %player% diamond 5");
        spiralCmds.add("give %player% golden_carrot 10");
        spiralCmds.add("give %player% experience_bottle 32");
        spiralCmds.add("give %player% ender_pearl 4");
        config.set("pools.spiral.commands", spiralCmds);
    }

    public List<ItemStack> getPrizesForAnimation(String animationType) {
        String pool = animationType.toLowerCase();
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
            meta.displayName(serializer.deserialize(ColorUtil.colorize("&d&l🎁 Unrevealed Reward")));

            List<Component> lore = new ArrayList<>();
            lore.add(serializer.deserialize(ColorUtil.colorize("&7Right-click to reveal!")));
            lore.add(serializer.deserialize(ColorUtil.colorize("&7Duration: &f" + durationMin + "m")));
            meta.lore(lore);

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

            if (meta.hasDisplayName()) {
                Component displayName = meta.displayName();
                if (displayName != null) {
                    data.put("name", serializer.serialize(displayName));
                }
            }

            if (meta.hasLore()) {
                List<Component> loreComponents = meta.lore();
                if (loreComponents != null) {
                    List<String> loreStrings = new ArrayList<>();
                    for (Component c : loreComponents) {
                        loreStrings.add(serializer.serialize(c));
                    }
                    data.put("lore", loreStrings);
                }
            }

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
            String matName = (String) data.get("material");
            Material material = Material.getMaterial(matName);
            if (material == null) material = Material.STONE;

            int amount = 1;
            if (data.get("amount") instanceof Number num) {
                amount = num.intValue();
            }

            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                if (data.containsKey("name")) {
                    String name = (String) data.get("name");
                    meta.displayName(serializer.deserialize(ColorUtil.colorize(name)));
                }

                if (data.containsKey("lore") && data.get("lore") instanceof List<?> list) {
                    List<Component> componentLore = new ArrayList<>();
                    for (Object line : list) {
                        if (line instanceof String s) {
                            componentLore.add(serializer.deserialize(ColorUtil.colorize(s)));
                        }
                    }
                    meta.lore(componentLore);
                }

                if (data.containsKey("enchantments") && data.get("enchantments") instanceof Map<?, ?> enchants) {
                    for (Map.Entry<?, ?> entry : enchants.entrySet()) {
                        if (entry.getKey() instanceof String key && entry.getValue() instanceof Integer level) {
                            NamespacedKey nsKey = NamespacedKey.minecraft(key);

                            Enchantment enchantment = Bukkit.getRegistry(Enchantment.class).get(nsKey);

                            if (enchantment != null) {
                                meta.addEnchant(enchantment, level, true);
                            }
                        }
                    }
                }
                item.setItemMeta(meta);
            }
            return item;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error restoring item from prizes.yml", e);
            return new ItemStack(Material.STONE);
        }
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save prizes.yml", e);
        }
    }
}