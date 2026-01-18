package com.rocketdev.oggiveaway;

import com.rocketdev.oggiveaway.commands.MainCommand;
import com.rocketdev.oggiveaway.config.ConfigManager;
import com.rocketdev.oggiveaway.gui.GUIListener;
import com.rocketdev.oggiveaway.listener.ConnectionListener;
import com.rocketdev.oggiveaway.listener.VoucherListener;
import com.rocketdev.oggiveaway.manager.*;
import com.rocketdev.oggiveaway.manager.ScheduleManager;
import com.rocketdev.oggiveaway.hook.GiveawayExpansion;
import com.rocketdev.oggiveaway.task.VoucherUpdateTask;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class OGGiveaway extends JavaPlugin {

    private static OGGiveaway instance;

    private ConfigManager configManager;
    private PrizeManager prizeManager;
    private GiveawayManager giveawayManager;
    private AnimationSettingsManager animationSettingsManager;
    private BossBarManager bossBarManager;
    private ScheduleManager scheduleManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        this.animationSettingsManager = new AnimationSettingsManager(this);
        this.bossBarManager = new BossBarManager(this);
        this.prizeManager = new PrizeManager(this);
        this.giveawayManager = new GiveawayManager(this);
        this.scheduleManager = new ScheduleManager(this);

        getCommand("giveaway").setExecutor(new MainCommand(this));
        getCommand("giveaway").setTabCompleter(new MainCommand(this));

        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VoucherListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);

        new VoucherUpdateTask(this).runTaskTimer(this, 0L, 20L);

        this.scheduleManager.startScheduler();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GiveawayExpansion(this).register();
        }

        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize("&8&m---------------------------------------------"));
        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize(" &b&lGiveawayOG &bv" + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize(" &fMade with &c❤ &fby &6Swagger Studio"));
        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize(" "));
        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize(" &a✔ Thanks for using our plugin!"));
        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize("&8&m---------------------------------------------"));
    }

    @Override
    public void onDisable() {
        if (giveawayManager != null) {
            giveawayManager.cancelGiveaway();
        }

        if (bossBarManager != null) {
            bossBarManager.removeAll();
        }

        if (scheduleManager != null) {
            scheduleManager.stopScheduler();
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            String title = p.getOpenInventory().getTitle();
            if (title.contains("GiveawayOG") || title.contains("Editing") || title.contains("Pool")) {
                p.closeInventory();
            }
        }

        Bukkit.getScheduler().cancelTasks(this);

        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize("&8&m---------------------------------------------"));
        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize(" &c&lGiveawayOG &cdisabled. Goodbye!"));
        Bukkit.getConsoleSender().sendMessage(ColorUtil.colorize("&8&m---------------------------------------------"));
    }

    public static OGGiveaway getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() { return configManager; }
    public PrizeManager getPrizeManager() { return prizeManager; }
    public GiveawayManager getGiveawayManager() { return giveawayManager; }
    public AnimationSettingsManager getAnimationSettingsManager() { return animationSettingsManager; }
    public BossBarManager getBossBarManager() { return bossBarManager; }
    public ScheduleManager getScheduleManager() { return scheduleManager; }
}