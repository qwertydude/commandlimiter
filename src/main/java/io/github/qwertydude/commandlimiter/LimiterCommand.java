package io.github.qwertydude.commandlimiter;

import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

/**
 * /commandlimiter reload|list|add <command>|remove <command>
 */
public final class LimiterCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = List.of("reload", "list", "add", "remove");

    private final CommandLimiter plugin;

    public LimiterCommand(CommandLimiter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.blockedCommands().reload();
                plugin.refreshAllCommandTrees();
                sender.sendMessage(Component.text("CommandLimiter config reloaded.", NamedTextColor.GREEN));
            }
            case "list" -> {
                sender.sendMessage(Component.text("Blocked commands: ", NamedTextColor.GOLD)
                        .append(Component.text(String.join(", ", plugin.blockedCommands().configuredNames()), NamedTextColor.WHITE)));
                sender.sendMessage(Component.text("Blocked labels (incl. aliases): ", NamedTextColor.GOLD)
                        .append(Component.text(String.join(", ", plugin.blockedCommands().allBlockedLabels()), NamedTextColor.GRAY)));
            }
            case "add" -> {
                if (args.length < 2) {
                    return false;
                }
                if (plugin.blockedCommands().addToConfig(args[1])) {
                    plugin.refreshAllCommandTrees();
                    sender.sendMessage(Component.text("Blocked /" + CommandNames.normalize(args[1]) + ".", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("/" + CommandNames.normalize(args[1]) + " is already blocked.", NamedTextColor.RED));
                }
            }
            case "remove" -> {
                if (args.length < 2) {
                    return false;
                }
                if (plugin.blockedCommands().removeFromConfig(args[1])) {
                    plugin.refreshAllCommandTrees();
                    sender.sendMessage(Component.text("Unblocked /" + CommandNames.normalize(args[1]) + ".", NamedTextColor.GREEN));
                } else {
                    sender.sendMessage(Component.text("/" + CommandNames.normalize(args[1]) + " is not in the blocked list.", NamedTextColor.RED));
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUB_COMMANDS.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            return plugin.blockedCommands().configuredNames().stream()
                    .filter(name -> name.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        }
        return List.of();
    }
}
