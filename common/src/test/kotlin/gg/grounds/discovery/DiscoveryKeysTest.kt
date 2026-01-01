package gg.grounds.discovery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DiscoveryKeysTest {
    @Test
    fun paperServerKeyBuildsExpectedPrefix() {
        assertEquals("grounds:server-discovery:paper:lobby", DiscoveryKeys.paperServerKey("lobby"))
    }

    @Test
    fun paperServerPatternUsesPrefixWildcard() {
        assertEquals("grounds:server-discovery:paper:*", DiscoveryKeys.paperServerPattern())
    }
}
