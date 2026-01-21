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

        if (args.length == 0) {
            sendHelpMenu(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "menu":
            case "gui":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("giveawayog.admin")) {
                        AdminGUI.openDashboard(player, plugin);
                    } else {
                        player.sendMessage(ColorUtil.colorize("&cNo permission."));
                    }
                } else {
                    sender.sendMessage(ColorUtil.colorize("&cThis command is for players only."));
                }
                break;

            case "start":
                if (sender.hasPermission("giveawayog.admin")) {
                    plugin.getGiveawayManager().startGiveaway(null);
                    sender.sendMessage(ColorUtil.colorize("&aGiveaway started successfully!"));
                } else {
                    sender.sendMessage(ColorUtil.colorize("&cNo permission."));
                }
                break;

            case "createvoucher":
            case "cv":
                if (sender.hasPermission("giveawayog.admin")) {
                    if (args.length < 3) {
                        sender.sendMessage(ColorUtil.colorize("&cUsage: /gw createvoucher <pool> <command>"));
                        sender.sendMessage(ColorUtil.colorize("&7Pools: blacksmith, spiral"));
                        return true;
                    }

                    String pool = args[1].toLowerCase();
                    if (!pool.equals("blacksmith") && !pool.equals("spiral")) {
                        sender.sendMessage(ColorUtil.colorize("&c⚠ Unknown pool '" + pool + "'. Using 'blacksmith' by default."));
                        pool = "blacksmith";
                    }

                    String cmdStr = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                    plugin.getPrizeManager().addCommandPrize(pool, cmdStr);
                    sender.sendMessage(ColorUtil.colorize("&a&l✔ Voucher added to '" + pool + "' pool!"));
                } else {
                    sender.sendMessage(ColorUtil.colorize("&cNo permission."));
                }
                break;

            case "support":
            case "discord":
            case "bug":
                sendSupportMessage(sender);
                break;

            case "reload":
                if (sender.hasPermission("giveawayog.admin")) {
                    plugin.getConfigManager().reload();
                    WebhookConfig.reload();
                    sender.sendMessage(ColorUtil.colorize("&a&l✔ Configuration & Webhooks Reloaded!"));
                }
                break;

            default:
                sender.sendMessage(ColorUtil.colorize("&cUnknown command. Try /giveaway"));
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

    private void sendHelpMenu(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&8&m-----------------------------"));
        sender.sendMessage(ColorUtil.colorize("&b&lGiveaway System &7- &fCommands"));
        sender.sendMessage(ColorUtil.colorize("&b/gw menu &7- Open Dashboard"));
        sender.sendMessage(ColorUtil.colorize("&b/gw start &7- Force start"));
        sender.sendMessage(ColorUtil.colorize("&b/gw cv <pool> <cmd> &7- Create voucher"));
        sender.sendMessage(ColorUtil.colorize("&b/gw reload &7- Reload config"));
        sender.sendMessage(ColorUtil.colorize("&b/gw support &7- Get help"));
        sender.sendMessage(ColorUtil.colorize("&8&m-----------------------------"));
    }

    private void sendSupportMessage(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&8&m-----------------------------"));
        sender.sendMessage(ColorUtil.colorize("&b&lSwagger Studio Support"));
        sender.sendMessage(ColorUtil.colorize("&7Found a bug? Need help? Join our Discord!"));

        if (sender instanceof Player) {
            TextComponent message = new TextComponent(ColorUtil.colorize("&9&nClick to Join Discord"));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/hZaR7zwH9Q"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open!").create()));
            ((Player) sender).spigot().sendMessage(message);
        } else {
            sender.sendMessage(ColorUtil.colorize("&9https://discord.gg/hZaR7zwH9Q"));
        }

        sender.sendMessage(ColorUtil.colorize("&8&m-----------------------------"));
    }
}