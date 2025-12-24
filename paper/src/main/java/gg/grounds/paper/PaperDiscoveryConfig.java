package gg.grounds.paper;

import gg.grounds.discovery.DiscoveryKeys;
import gg.grounds.discovery.PaperServerEntry;
import gg.grounds.discovery.ValkeyConfig;

import java.util.Optional;
import java.util.logging.Logger;

public record PaperDiscoveryConfig(
        DiscoveryKeys discoveryKeys,
        ValkeyConfig valkeyConfig,
        PaperServerEntry baseEntry
) {
    private static final String ENV_VALKEY_HOST = "VALKEY_HOST";
    private static final String ENV_VALKEY_PORT = "VALKEY_PORT";
    private static final String ENV_SERVER_NAME = "SERVER_NAME";
    private static final String ENV_POD_IP = "POD_IP";

    public static PaperDiscoveryConfig load(Logger logger) {
        DiscoveryKeys keys = new DiscoveryKeys();

        String valkeyHost = Optional.ofNullable(System.getenv(ENV_VALKEY_HOST)).orElse("localhost");
        int valkeyPort = Integer.parseInt(Optional.ofNullable(System.getenv(ENV_VALKEY_PORT)).orElse("6379"));

        ValkeyConfig valkeyConfig = new ValkeyConfig(valkeyHost, valkeyPort);

        PaperServerEntry baseEntry = new PaperServerEntry(
                Optional.ofNullable(System.getenv(ENV_SERVER_NAME)).orElseThrow(
                        () -> new IllegalStateException("Environment variable SERVER_NAME is not set")
                ),
                Optional.ofNullable(System.getenv(ENV_POD_IP)).orElseThrow(
                        () -> new IllegalStateException("Environment variable POD_IP is not set")
                ),
                25565
        );
        return new PaperDiscoveryConfig(keys, valkeyConfig, baseEntry);
    }
}
