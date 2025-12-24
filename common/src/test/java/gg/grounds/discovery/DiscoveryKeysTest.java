package gg.grounds.discovery;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiscoveryKeysTest {
    private final DiscoveryKeys keys = new DiscoveryKeys();

    @Test
    void paperServerKeyBuildsExpectedPrefix() {
        assertEquals("grounds:server-discovery:paper:lobby", keys.paperServerKey("lobby"));
    }

    @Test
    void paperServerKeyRejectsNull() {
        assertThrows(NullPointerException.class, () -> keys.paperServerKey(null));
    }

    @Test
    void paperServerPatternUsesPrefixWildcard() {
        assertEquals("grounds:server-discovery:paper:*", keys.paperServerPattern());
    }
}
