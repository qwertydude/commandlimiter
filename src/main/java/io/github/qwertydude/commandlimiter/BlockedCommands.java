package io.github.qwertydude.commandlimiter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

/**
 * Holds the configured blocked-command list and answers "is this command
 * blocked for this player?". Permission checks go through the regular Bukkit
 * permission API, so any permission plugin (LuckPerms etc.) works.
 */
public final class BlockedCommands {

    public static final String BYPASS_ALL = "commandlimiter.bypass";
    public static final String BYPASS_PREFIX = "commandlimiter.bypass.";

    private final CommandLimiter plugin;

    /** Base names exactly as configured (normalized). */
    private final Set<String> configured = new LinkedHashSet<>();
    /** Every blocked label (configured names + resolved aliases) -> configured base name. */
    private final Map<String, String> blockedLabels = new TreeMap<>();

    private Component blockedMessage = Component.empty();

    public BlockedCommands(CommandLimiter plugin) {
        this.plugin = plugin;
    }

    /**
     * Re-reads the config and rebuilds the blocked label map, resolving
     * registered aliases of each blocked command when block-aliases is on.
     */
    public synchronized void reload() {
        configured.clear();
        blockedLabels.clear();

        for (String entry : plugin.getConfig().getStringList("blocked-commands")) {
            String name = CommandNames.normalize(entry);
            if (!name.isEmpty()) {
                configured.add(name);
            }
        }

        boolean blockAliases = plugin.getConfig().getBoolean("block-aliases", true);
        for (String name : configured) {
            blockedLabels.put(name, name);
            if (!blockAliases) {
                continue;
            }
            Command command = plugin.getServer().getCommandMap().getCommand(name);
            if (command == null) {
                continue;
            }
            blockedLabels.put(command.getName().toLowerCase(Locale.ROOT), name);
            for (String alias : command.getAliases()) {
                blockedLabels.put(CommandNames.normalize(alias), name);
            }
        }

        String rawMessage = plugin.getConfig().getString("blocked-message", "&cYou cannot use this command.");
        blockedMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(rawMessage);
    }

    /**
     * @param label a command label as typed or as sent to the client,
     *              with or without slash/namespace ("tp", "/tp", "minecraft:tp")
     */
    public synchronized boolean isBlockedFor(Player player, String label) {
        String base = CommandNames.normalize(label);
        String canonical = blockedLabels.get(base);
        if (canonical == null) {
            return false;
        }
        if (player.hasPermission(BYPASS_ALL)) {
            return false;
        }
        return !player.hasPermission(BYPASS_PREFIX + canonical)
                && !player.hasPermission(BYPASS_PREFIX + base);
    }

    public Component blockedMessage() {
        return blockedMessage;
    }

    public synchronized List<String> configuredNames() {
        return new ArrayList<>(configured);
    }

    public synchronized Set<String> allBlockedLabels() {
        return new TreeSet<>(blockedLabels.keySet());
    }

    /** Adds a command to the config list. Returns false if it was already present. */
    public synchronized boolean addToConfig(String name) {
        String base = CommandNames.normalize(name);
        if (base.isEmpty()) {
            return false;
        }
        List<String> list = new ArrayList<>(plugin.getConfig().getStringList("blocked-commands"));
        if (list.stream().anyMatch(entry -> CommandNames.normalize(entry).equals(base))) {
            return false;
        }
        list.add(base);
        plugin.getConfig().set("blocked-commands", list);
        plugin.saveConfig();
        reload();
        return true;
    }

    /** Removes a command from the config list. Returns false if it was not present. */
    public synchronized boolean removeFromConfig(String name) {
        String base = CommandNames.normalize(name);
        List<String> list = new ArrayList<>(plugin.getConfig().getStringList("blocked-commands"));
        boolean changed = list.removeIf(entry -> CommandNames.normalize(entry).equals(base));
        if (!changed) {
            return false;
        }
        plugin.getConfig().set("blocked-commands", list);
        plugin.saveConfig();
        reload();
        return true;
    }
}
