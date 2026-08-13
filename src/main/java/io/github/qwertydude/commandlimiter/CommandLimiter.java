package io.github.qwertydude.commandlimiter;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommandLimiter extends JavaPlugin {

    private BlockedCommands blockedCommands;
    private LuckPermsHook luckPermsHook;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        blockedCommands = new BlockedCommands(this);
        blockedCommands.reload();

        getServer().getPluginManager().registerEvents(new CommandListener(this), this);

        PluginCommand command = getCommand("commandlimiter");
        LimiterCommand executor = new LimiterCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            try {
                luckPermsHook = LuckPermsHook.register(this);
                getLogger().info("Hooked into LuckPerms: command trees refresh on permission changes.");
            } catch (Throwable t) {
                getLogger().warning("Could not hook into LuckPerms (" + t.getMessage()
                        + "); players need to rejoin for permission changes to update their tab list.");
            }
        }

        getLogger().info("Blocking " + blockedCommands.configuredNames().size() + " command(s) for players without bypass permission.");
    }

    @Override
    public void onDisable() {
        if (luckPermsHook != null) {
            luckPermsHook.close();
            luckPermsHook = null;
        }
    }

    public BlockedCommands blockedCommands() {
        return blockedCommands;
    }

    /** Resends the command tree to every online player so hidden commands update live. */
    public void refreshAllCommandTrees() {
        for (Player player : getServer().getOnlinePlayers()) {
            player.updateCommands();
        }
    }
}
