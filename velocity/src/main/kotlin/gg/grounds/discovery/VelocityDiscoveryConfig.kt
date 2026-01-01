package gg.grounds.discovery

data class VelocityDiscoveryConfig(val valkeyConfig: ValkeyConfig) {
    companion object {
        private const val ENV_VALKEY_HOST = "VALKEY_HOST"
        private const val ENV_VALKEY_PORT = "VALKEY_PORT"

        @JvmStatic
        fun load(): VelocityDiscoveryConfig {
            val valkeyHost = System.getenv(ENV_VALKEY_HOST) ?: "localhost"
            val valkeyPort = (System.getenv(ENV_VALKEY_PORT) ?: "6379").toInt()

            val valkeyConfig = ValkeyConfig(valkeyHost, valkeyPort)

            return VelocityDiscoveryConfig(valkeyConfig)
        }
    }
}
