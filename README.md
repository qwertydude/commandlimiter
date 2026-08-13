# CommandLimiter

A lightweight Purpur/Paper **1.21.11** plugin that hides restricted commands from players' tab completion and blocks them from running those commands. Permissions are checked through the standard Bukkit permission API, so it works out of the box with **LuckPerms**.

## What it does

- **Hides blocked commands from tab completion.** Players without permission never see `/tp`, `/gamemode`, etc. in their command list — including namespaced forms like `/minecraft:tp` and registered aliases.
- **Blocks execution.** If a player types a blocked command anyway, it is cancelled and they get a configurable message (default: `You cannot use this command.`).
- **Live updates with LuckPerms.** When LuckPerms recalculates a player's permissions (e.g. after `/lp user ... permission set ...`), the player's visible command list refreshes immediately — no rejoin needed.

Console, command blocks, and players with bypass permission are unaffected.

## Installation

1. Download `CommandLimiter-1.0.0.jar` from the [release](release/) folder (or the GitHub Releases page) and drop it into your server's `plugins/` folder.
2. Restart the server.
3. Edit `plugins/CommandLimiter/config.yml` to taste and run `/commandlimiter reload`.

## Configuration (`config.yml`)

```yaml
blocked-commands:      # base names, no slash; namespaced forms are covered automatically
- tp
- teleport
- gamemode
- give
- op
- deop
- stop
- reload
- plugins
- pl
- version
- ver
- about
- seed

block-aliases: true    # also hide/block registered aliases of each command

blocked-message: "&cYou cannot use this command."   # supports & color codes
```

## Permissions

| Permission | Default | Description |
|---|---|---|
| `commandlimiter.bypass` | op | See and use every blocked command |
| `commandlimiter.bypass.<command>` | — | See and use one blocked command (e.g. `commandlimiter.bypass.tp`) |
| `commandlimiter.admin` | op | Access to `/commandlimiter` |

### LuckPerms examples

```
/lp user Steve permission set commandlimiter.bypass.tp true
/lp group mod permission set commandlimiter.bypass true
```

Changes apply instantly — CommandLimiter listens to LuckPerms recalculation events and resends the command tree.

## Commands

| Command | Description |
|---|---|
| `/commandlimiter reload` | Reload the config |
| `/commandlimiter list` | Show blocked commands and all blocked labels/aliases |
| `/commandlimiter add <command>` | Block a command and save it to the config |
| `/commandlimiter remove <command>` | Unblock a command |

Aliases: `/cmdlimiter`, `/climit`

## Building from source

Requires JDK 21 and Maven:

```
mvn package
```

The jar lands in `target/CommandLimiter-1.0.0.jar`. The plugin is compiled against `paper-api 1.21.11-R0.1-SNAPSHOT` (Purpur is a Paper fork, so this is the standard compile target for Purpur plugins) and the LuckPerms API.
