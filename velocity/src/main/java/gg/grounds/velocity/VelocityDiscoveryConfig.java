package gg.grounds.velocity;

import gg.grounds.discovery.DiscoveryKeys;
import gg.grounds.discovery.ValkeyConfig;

import java.util.Optional;

public record VelocityDiscoveryConfig(
        DiscoveryKeys discoveryKeys,
        ValkeyConfig valkeyConfig
) {
    private static final String ENV_VALKEY_HOST = "VALKEY_HOST";
    private static final String ENV_VALKEY_PORT = "VALKEY_PORT";

    public static VelocityDiscoveryConfig load() {
        DiscoveryKeys keys = new DiscoveryKeys();

        String valkeyHost = Optional.ofNullable(System.getenv(ENV_VALKEY_HOST)).orElse("localhost");
        int valkeyPort = Integer.parseInt(Optional.ofNullable(System.getenv(ENV_VALKEY_PORT)).orElse("6379"));

        ValkeyConfig valkeyConfig = new ValkeyConfig(valkeyHost, valkeyPort);

        return new VelocityDiscoveryConfig(keys, valkeyConfig);
    }
}
