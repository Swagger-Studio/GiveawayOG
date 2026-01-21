package com.rocketdev.oggiveaway.commands;



import com.rocketdev.oggiveaway.config.WebhookConfig;
import com.rocketdev.oggiveaway.OGGiveaway;
import com.rocketdev.oggiveaway.gui.AdminGUI;
import com.rocketdev.oggiveaway.utils.ColorUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainCommand implements TabExecutor {

    private final OGGiveaway plugin;

    public MainCommand(OGGiveaway plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMenu(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "menu":
            case "gui":
                if (player.hasPermission("giveawayog.admin")) {
                    AdminGUI.openDashboard(player, plugin);
                } else {
                    player.sendMessage(ColorUtil.colorize("&cNo permission."));
                }
                break;

            case "start":
                if (player.hasPermission("giveawayog.admin")) {
                    plugin.getGiveawayManager().startGiveaway(null);
                } else {
                    player.sendMessage(ColorUtil.colorize("&cNo permission."));
                }
                break;

            case "createvoucher":
            case "cv":
                if (player.hasPermission("giveawayog.admin")) {
                    if (args.length < 3) {
                        player.sendMessage(ColorUtil.colorize("&cUsage: /gw createvoucher <pool> <command>"));
                        player.sendMessage(ColorUtil.colorize("&7Pools: blacksmith, spiral"));
                        return true;
                    }

                    String pool = args[1].toLowerCase();
                    if (!pool.equals("blacksmith") && !pool.equals("spiral")) {
                        player.sendMessage(ColorUtil.colorize("&c⚠ Unknown pool '" + pool + "'. Using 'blacksmith' by default."));
                        pool = "blacksmith";
                    }

                    String cmdStr = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                    plugin.getPrizeManager().addCommandPrize(pool, cmdStr);
                    player.sendMessage(ColorUtil.colorize("&a&l✔ Voucher added to '" + pool + "' pool!"));
                } else {
                    player.sendMessage(ColorUtil.colorize("&cNo permission."));
                }
                break;

            case "support":
            case "discord":
            case "bug":
                sendSupportMessage(player);
                break;

            case "reload":
                if (player.hasPermission("giveawayog.admin")) {
                    plugin.getConfigManager().reload();
                    WebhookConfig.reload();
                    player.sendMessage(ColorUtil.colorize("&a&l✔ Configuration & Webhooks Reloaded!"));
                }
                break;

            default:
                player.sendMessage(ColorUtil.colorize("&cUnknown command. Try /giveaway"));
        }

        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("giveawayog.admin")) {
                completions.add("start");
                completions.add("menu");
                completions.add("createvoucher");
                completions.add("reload");
            }
            completions.add("support");
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("createvoucher") || args[0].equalsIgnoreCase("cv")) {
                completions.add("blacksmith");
                completions.add("spiral");
            }
        }

        return completions;
    }

    private void sendHelpMenu(Player player) {
        player.sendMessage(ColorUtil.colorize("&8&m-----------------------------"));
        player.sendMessage(ColorUtil.colorize("&b&lGiveaway System &7- &fCommands"));
        player.sendMessage(ColorUtil.colorize("&b/gw menu &7- Open Dashboard"));
        player.sendMessage(ColorUtil.colorize("&b/gw start &7- Force start"));
        player.sendMessage(ColorUtil.colorize("&b/gw cv <pool> <cmd> &7- Create voucher"));
        player.sendMessage(ColorUtil.colorize("&b/gw reload &7- Reload config"));
        player.sendMessage(ColorUtil.colorize("&b/gw support &7- Get help"));
        player.sendMessage(ColorUtil.colorize("&8&m-----------------------------"));
    }

    private void sendSupportMessage(Player player) {
        player.sendMessage(ColorUtil.colorize("&8&m-----------------------------"));
        player.sendMessage(ColorUtil.colorize("&b&lSwagger Studio Support"));
        player.sendMessage(ColorUtil.colorize("&7Found a bug? Need help? Join our Discord!"));

        TextComponent message = new TextComponent(ColorUtil.colorize("&9&nClick to Join Discord"));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/hZaR7zwH9Q"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open!").create()));

        player.spigot().sendMessage(message);
        player.sendMessage(ColorUtil.colorize("&8&m-----------------------------"));
    }
}