package gg.grounds.discovery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PaperServerEntryTest {

    @Test
    fun encodeFormatsWithPipes() {
        val entry = PaperServerEntry("lobby", "127.0.0.1", 25565)
        assertEquals("lobby|127.0.0.1|25565", entry.encode())
    }

    @Test
    fun decodeTrimsAndParsesValues() {
        val decoded = PaperServerEntry.decode(" lobby | 127.0.0.1 | 25565 ")
        assertEquals(PaperServerEntry("lobby", "127.0.0.1", 25565), decoded)
    }

    @Test
    fun decodeRejectsMissingOrInvalidValues() {
        assertNull(PaperServerEntry.decode(null))
        assertNull(PaperServerEntry.decode("   "))
        assertNull(PaperServerEntry.decode("one|two"))
        assertNull(PaperServerEntry.decode("||25565"))
        assertNull(PaperServerEntry.decode("lobby||25565"))
        assertNull(PaperServerEntry.decode("lobby|host|not-a-number"))
    }
}
