package gg.grounds.discovery

data class PaperDiscoveryConfig(val valkeyConfig: ValkeyConfig, val baseEntry: PaperServerEntry) {
    companion object {
        private const val ENV_VALKEY_HOST = "VALKEY_HOST"
        private const val ENV_VALKEY_PORT = "VALKEY_PORT"
        private const val ENV_SERVER_NAME = "SERVER_NAME"
        private const val ENV_POD_IP = "POD_IP"

        @JvmStatic
        fun load(): PaperDiscoveryConfig {
            val valkeyHost = System.getenv(ENV_VALKEY_HOST) ?: "localhost"
            val valkeyPort = (System.getenv(ENV_VALKEY_PORT) ?: "6379").toInt()

            val valkeyConfig = ValkeyConfig(valkeyHost, valkeyPort)

            val baseEntry =
                PaperServerEntry(
                    System.getenv(ENV_SERVER_NAME)
                        ?: throw IllegalStateException(
                            "Environment variable SERVER_NAME is not set"
                        ),
                    System.getenv(ENV_POD_IP)
                        ?: throw IllegalStateException("Environment variable POD_IP is not set"),
                    25565,
                )
            return PaperDiscoveryConfig(valkeyConfig, baseEntry)
        }
    }
}
