package gg.grounds.discovery;

import java.util.Objects;

public final class DiscoveryKeys {
    private static final String PAPER_PREFIX = "grounds:server-discovery:paper:";

    public String paperServerKey(String serverName) {
        Objects.requireNonNull(serverName, "serverName");
        return PAPER_PREFIX + serverName;
    }

    public String paperServerPattern() {
        return PAPER_PREFIX + "*";
    }
}
