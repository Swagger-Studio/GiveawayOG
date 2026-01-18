package com.rocketdev.oggiveaway.manager;

import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.animation.AnimationFactory;
import com.rocketdev.oggiveaway.animation.WinnerRevealTask;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import com.rocketdev.oggiveaway.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class GiveawayManager {
    private final OGGiveaway plugin;
    private final Random random = new Random();
    private final BlacksmithManager blacksmithManager;

    public static boolean isRunning = false;
    private UUID currentWinnerId;
    private String selectedAnimationType;
    private List<ItemStack> currentPrizes;

    private final List<BukkitTask> activeTasks = new ArrayList<>();

    public GiveawayManager(OGGiveaway plugin) {
        this.plugin = plugin;
        this.blacksmithManager = new BlacksmithManager(plugin);
    }

    public void startGiveaway(List<ItemStack> manualOverride) {
        if (isRunning) {
            LoggerUtil.broadcast("&c⚠ A giveaway is already in progress! Please wait.");
            return;
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        int minPlayers = plugin.getConfig().getInt("settings.min-players", 1);

        if (players.size() < minPlayers) {
            LoggerUtil.broadcast("&c⚠ Not enough players! Need at least " + minPlayers + ".");
            return;
        }

        isRunning = true;
        activeTasks.clear();

        Player winner = players.get(random.nextInt(players.size()));
        currentWinnerId = winner.getUniqueId();

        this.selectedAnimationType = plugin.getAnimationSettingsManager().pickRandomAnimation();

        if (manualOverride != null && !manualOverride.isEmpty() && manualOverride.get(0).getType() != Material.DIAMOND) {
            this.currentPrizes = manualOverride;
        } else {
            this.currentPrizes = plugin.getPrizeManager().getPrizesForAnimation(selectedAnimationType);
        }

        if (this.currentPrizes.isEmpty()) {
            this.currentPrizes = Collections.singletonList(new ItemStack(Material.DIAMOND, 1));
        }

        List<String> startMsgs = plugin.getConfig().getStringList("messages.broadcast.start");
        if (!startMsgs.isEmpty()) {
            LoggerUtil.broadcastBlock(startMsgs.toArray(new String[0]));
        }
        plugin.getBossBarManager().startGiveawayBar();

        for(Player p : Bukkit.getOnlinePlayers()) {
            boolean isWinner = p.getUniqueId().equals(currentWinnerId);
            BukkitTask task = AnimationFactory.getSlotTask(plugin, p, currentPrizes, isWinner).runTaskTimer(plugin, 0L, 1L);
            activeTasks.add(task);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        }
    }

    public void onAnimationFinish(Player winner) {
        if (!isRunning) return;

        List<String> winMsgs = plugin.getConfig().getStringList("messages.broadcast.winner");
        if (!winMsgs.isEmpty()) {
            List<String> processedMsgs = new ArrayList<>();
            for (String line : winMsgs) processedMsgs.add(line.replace("%player%", winner.getName()));
            LoggerUtil.broadcastBlock(processedMsgs.toArray(new String[0]));
        }

        List<ItemStack> activePrizes = new ArrayList<>();
        for (ItemStack item : currentPrizes) {
            activePrizes.add(stampVoucher(item.clone()));
        }

        if (selectedAnimationType.equals("BLACKSMITH")) {
            blacksmithManager.startForgeEvent(winner, activePrizes);
        } else {
            new WinnerRevealTask(plugin, winner).runTaskTimer(plugin, 0L, 2L);
            ItemStack prize = activePrizes.get(random.nextInt(activePrizes.size()));
            winner.getInventory().addItem(prize);
            endGiveaway();
        }
    }

    public void cancelGiveaway() {
        if (!isRunning) return;

        for (BukkitTask task : activeTasks) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activeTasks.clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Entity e : p.getNearbyEntities(5, 5, 5)) {
                if (e instanceof ItemDisplay || e.getType().name().contains("DISPLAY")) {
                    e.remove();
                }
            }
        }

        plugin.getBossBarManager().removeAll();
        if (currentWinnerId != null) {
            blacksmithManager.cleanup(currentWinnerId);
        }

        isRunning = false;
        currentWinnerId = null;

        LoggerUtil.broadcast("&c&l⛔ Giveaway Cancelled by Admin!");
    }

    public void handleDisconnect(Player player) {
        if (isRunning && currentWinnerId != null && player.getUniqueId().equals(currentWinnerId)) {
            LoggerUtil.broadcast("&c⚠ Winner left! Cancelled.");
            cancelGiveaway();
        }
    }

    public void endGiveaway() {
        isRunning = false;
        currentWinnerId = null;
        activeTasks.clear();
    }

    private ItemStack stampVoucher(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return item;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey durKey = new NamespacedKey(plugin, "voucher_duration");
        NamespacedKey expKey = new NamespacedKey(plugin, "expiry");

        if (meta.getPersistentDataContainer().has(durKey, PersistentDataType.INTEGER)) {
            int minutes = meta.getPersistentDataContainer().get(durKey, PersistentDataType.INTEGER);
            long expiryTime = System.currentTimeMillis() + (minutes * 60 * 1000L);
            meta.getPersistentDataContainer().set(expKey, PersistentDataType.LONG, expiryTime);

            List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 2) {
                lore.set(1, ColorUtil.colorize("&7Expires in: &e" + minutes + "m 00s"));
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
