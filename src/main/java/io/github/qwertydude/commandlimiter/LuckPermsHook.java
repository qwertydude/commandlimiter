package io.github.qwertydude.commandlimiter;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.entity.Player;

/**
 * Optional LuckPerms integration. When a user's permissions are recalculated
 * (e.g. /lp user X permission set commandlimiter.bypass.tp true), the client's
 * command tree is refreshed immediately so hidden commands appear/disappear
 * without a rejoin.
 *
 * Only loaded when the LuckPerms plugin is present, so the LuckPerms API
 * classes are never touched otherwise.
 */
final class LuckPermsHook {

    private final EventSubscription<UserDataRecalculateEvent> subscription;

    private LuckPermsHook(CommandLimiter plugin) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        this.subscription = luckPerms.getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, event -> {
            Player player = plugin.getServer().getPlayer(event.getUser().getUniqueId());
            if (player != null && player.isOnline()) {
                // LuckPerms events can fire async; updateCommands must run on the main thread.
                plugin.getServer().getScheduler().runTask(plugin, player::updateCommands);
            }
        });
    }

    static LuckPermsHook register(CommandLimiter plugin) {
        return new LuckPermsHook(plugin);
    }

    void close() {
        subscription.close();
    }
}
