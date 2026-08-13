package io.github.qwertydude.commandlimiter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandNamesTest {

    @Test
    void normalizeStripsSlashNamespaceAndCase() {
        assertEquals("tp", CommandNames.normalize("tp"));
        assertEquals("tp", CommandNames.normalize("/tp"));
        assertEquals("tp", CommandNames.normalize("/TP"));
        assertEquals("tp", CommandNames.normalize("minecraft:tp"));
        assertEquals("tp", CommandNames.normalize("/Minecraft:TP"));
        assertEquals("tp", CommandNames.normalize(" /tp "));
        assertEquals("", CommandNames.normalize("/"));
        assertEquals("", CommandNames.normalize(""));
    }

    @Test
    void labelFromCommandLineTakesFirstToken() {
        assertEquals("tp", CommandNames.labelFromCommandLine("/tp Steve 0 64 0"));
        assertEquals("tp", CommandNames.labelFromCommandLine("/minecraft:tp Steve"));
        assertEquals("gamemode", CommandNames.labelFromCommandLine("/gamemode creative"));
        assertEquals("tp", CommandNames.labelFromCommandLine("/tp"));
        assertEquals("", CommandNames.labelFromCommandLine("hello world"));
        assertEquals("", CommandNames.labelFromCommandLine("/"));
        assertEquals("", CommandNames.labelFromCommandLine(""));
    }
}
