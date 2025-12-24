package gg.grounds.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class PaperServerEntryTest {

    @Test
    void encodeFormatsWithPipes() {
        PaperServerEntry entry = new PaperServerEntry("lobby", "127.0.0.1", 25565);
        assertEquals("lobby|127.0.0.1|25565", entry.encode());
    }

    @Test
    void decodeTrimsAndParsesValues() {
        Optional<PaperServerEntry> decoded = PaperServerEntry.decode(" lobby | 127.0.0.1 | 25565 ");
        assertTrue(decoded.isPresent());
        assertEquals(new PaperServerEntry("lobby", "127.0.0.1", 25565), decoded.get());
    }

    @Test
    void decodeRejectsMissingOrInvalidValues() {
        assertTrue(PaperServerEntry.decode(null).isEmpty());
        assertTrue(PaperServerEntry.decode("   ").isEmpty());
        assertTrue(PaperServerEntry.decode("one|two").isEmpty());
        assertTrue(PaperServerEntry.decode("||25565").isEmpty());
        assertTrue(PaperServerEntry.decode("lobby||25565").isEmpty());
        assertTrue(PaperServerEntry.decode("lobby|host|not-a-number").isEmpty());
    }
}
