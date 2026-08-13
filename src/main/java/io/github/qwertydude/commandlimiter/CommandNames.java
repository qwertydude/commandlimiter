package io.github.qwertydude.commandlimiter;

import java.util.Locale;

/**
 * Helpers for turning raw command input into a comparable base name.
 */
public final class CommandNames {

    private CommandNames() {
    }

    /**
     * Normalizes a command label to its lowercase base name: strips a leading
     * slash and any namespace prefix, e.g. "/Minecraft:TP" -> "tp".
     */
    public static String normalize(String label) {
        String name = label.trim().toLowerCase(Locale.ROOT);
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        int colon = name.lastIndexOf(':');
        if (colon >= 0) {
            name = name.substring(colon + 1);
        }
        return name;
    }

    /**
     * Extracts the command label from a full command line such as
     * "/tp Steve 0 64 0" or a tab-completion buffer. Returns "" if the
     * input is not a command.
     */
    public static String labelFromCommandLine(String line) {
        String text = line.trim();
        if (!text.startsWith("/")) {
            return "";
        }
        int space = text.indexOf(' ');
        String label = space >= 0 ? text.substring(0, space) : text;
        return normalize(label);
    }
}
