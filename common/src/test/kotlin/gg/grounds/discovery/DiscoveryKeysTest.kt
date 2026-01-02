package gg.grounds.discovery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DiscoveryKeysTest {
    @Test
    fun paperServersHashKeyMatchesExpectedValue() {
        assertEquals("grounds:server-discovery:paper", DiscoveryKeys.paperServersHashKey())
    }
}
