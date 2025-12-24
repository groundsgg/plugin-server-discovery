package gg.grounds.discovery;

import java.util.Optional;

public record PaperServerEntry(String name, String host, int port) {

    public String encode() {
        return name + "|" + host + "|" + port;
    }

    public static Optional<PaperServerEntry> decode(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String[] parts = value.split("\\|", -1);
        if (parts.length != 3) {
            return Optional.empty();
        }

        String name = parts[0].trim();
        String host = parts[1].trim();
        if (name.isEmpty() || host.isEmpty()) {
            return Optional.empty();
        }

        int port;
        try {
            port = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        try {
            return Optional.of(new PaperServerEntry(name, host, port));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
