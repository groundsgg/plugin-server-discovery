package gg.grounds.discovery

object DiscoveryKeys {
    private const val PAPER_PREFIX = "grounds:server-discovery:paper:"

    fun paperServerKey(serverName: String): String = "$PAPER_PREFIX$serverName"

    fun paperServerPattern(): String = "$PAPER_PREFIX*"
}
