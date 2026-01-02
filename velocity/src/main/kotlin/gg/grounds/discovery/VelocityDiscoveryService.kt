package gg.grounds.discovery

import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.ServerInfo
import com.velocitypowered.api.scheduler.ScheduledTask
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import org.slf4j.Logger

class VelocityDiscoveryService(
    private val proxyServer: ProxyServer,
    private val logger: Logger,
    config: VelocityDiscoveryConfig,
) {
    private val client = ValkeyClient(config.valkeyConfig)
    private var pollTask: ScheduledTask? = null

    fun start(plugin: Any) {
        refreshServers()
        pollTask =
            proxyServer.scheduler
                .buildTask(plugin, this::refreshServers)
                .repeat(1, TimeUnit.SECONDS)
                .schedule()
    }

    fun stop() {
        pollTask?.cancel()

        try {
            client.close()
        } catch (e: Exception) {
            logger.warn("Failed to close Valkey client: {}", e.message)
        }
    }

    private fun refreshServers() {
        val entries =
            try {
                client.hgetAll(DiscoveryKeys.paperServersHashKey())
            } catch (e: Exception) {
                logger.warn("Failed to load discovery entries from Valkey: {}", e.message)
                return
            }

        val now = System.currentTimeMillis()
        val staleThresholdMillis = TimeUnit.SECONDS.toMillis(15)
        val servers = HashSet<String>()

        for ((field, value) in entries) {
            val parsed = PaperServerEntry.decode(value)
            if (parsed == null) {
                logger.warn("Invalid discovery entry for field {}", field)
                continue
            }
            val lastSeen = parsed.lastSeenMillis
            if (lastSeen <= 0L || now - lastSeen > staleThresholdMillis) {
                logger.info("Removing stale discovery entry for server {}", parsed.name)
                client.hdel(DiscoveryKeys.paperServersHashKey(), field)
                continue
            }
            servers.add(parsed.name)
            registerOrUpdate(parsed)
        }

        unregisterMissing(servers)
    }

    private fun unregisterMissing(aliveNames: Set<String>) {
        for (existing in proxyServer.allServers) {
            if (!aliveNames.contains(existing.serverInfo.name)) {
                proxyServer.unregisterServer(existing.serverInfo)
                logger.info("Unregistered stale server {}", existing.serverInfo.name)
            }
        }
    }

    private fun registerOrUpdate(entry: PaperServerEntry) {
        val name = entry.name
        val address = InetSocketAddress.createUnresolved(entry.host, entry.port)
        val desiredInfo = ServerInfo(name, address)

        val proxyExisting = proxyServer.getServer(name)
        if (proxyExisting.isPresent) {
            val existingInfo = proxyExisting.get().serverInfo
            if (existingInfo.address != address) {
                logger.warn(
                    "Skipping discovered server {} because it conflicts with an existing registration",
                    name,
                )
                return
            }
            return
        }

        proxyServer.registerServer(desiredInfo)
        logger.info("Registered discovered server {}", name)
    }
}
