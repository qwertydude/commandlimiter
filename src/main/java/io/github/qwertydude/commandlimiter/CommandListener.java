package io.github.qwertydude.commandlimiter;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.server.TabCompleteEvent;

public final class CommandListener implements Listener {

    private final CommandLimiter plugin;

    public CommandListener(CommandLimiter plugin) {
        this.plugin = plugin;
    }

    /**
     * Removes blocked commands from the command tree sent to the client, so
     * they never show up in tab completion. Fired on join and whenever
     * Player#updateCommands() is called.
     */
    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(BlockedCommands.BYPASS_ALL)) {
            return;
        }
        event.getCommands().removeIf(label -> plugin.blockedCommands().isBlockedFor(player, label));
    }

    /**
     * Blocks execution of restricted commands. LOWEST priority so the command
     * is cancelled before other plugins process it.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String label = CommandNames.labelFromCommandLine(event.getMessage());
        if (label.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        if (plugin.blockedCommands().isBlockedFor(player, label)) {
            event.setCancelled(true);
            player.sendMessage(plugin.blockedCommands().blockedMessage());
        }
    }

    /**
     * Backstop for clients that still ask the server to complete arguments of
     * a blocked command (the command itself is already hidden client-side).
     */
    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player player)) {
            return;
        }
        String label = CommandNames.labelFromCommandLine(event.getBuffer());
        if (!label.isEmpty() && plugin.blockedCommands().isBlockedFor(player, label)) {
            event.setCancelled(true);
        }
    }

    /**
     * All plugins have registered their commands once the server finishes
     * loading (also fires after /reload); rebuild so alias resolution sees
     * every registered command.
     */
    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        plugin.blockedCommands().reload();
        plugin.refreshAllCommandTrees();
    }
}
