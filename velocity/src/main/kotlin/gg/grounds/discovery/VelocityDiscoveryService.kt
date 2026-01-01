package gg.grounds.discovery

import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import com.velocitypowered.api.proxy.server.ServerInfo
import com.velocitypowered.api.scheduler.ScheduledTask
import org.slf4j.Logger
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class VelocityDiscoveryService(
    private val proxyServer: ProxyServer,
    private val logger: Logger,
    config: VelocityDiscoveryConfig,
) {
    private val client = ValkeyClient(config.valkeyConfig)
    private val registeredServers: MutableMap<String, ServerInfo> = ConcurrentHashMap()
    private var pollTask: ScheduledTask? = null

    fun start(plugin: Any) {
        refreshServers()
        pollTask =
            proxyServer.scheduler
                .buildTask(plugin, this::refreshServers)
                .repeat(5, TimeUnit.SECONDS)
                .schedule()
    }

    fun stop() {
        pollTask?.cancel()
        registeredServers.values.forEach { info -> proxyServer.unregisterServer(info) }
        registeredServers.clear()
        try {
            client.close()
        } catch (e: Exception) {
            logger.warn("Failed to close Valkey client: {}", e.message)
        }
    }

    fun pickInitialServer(): Optional<RegisteredServer> {
        return registeredServers.values.firstOrNull()?.let { proxyServer.getServer(it.name) }
            ?: Optional.empty()
    }

    private fun refreshServers() {
        val entries =
            try {
                client.scanValues(DiscoveryKeys.paperServerPattern())
            } catch (e: Exception) {
                logger.warn("Failed to scan Valkey for servers: {}", e.message)
                return
            }

        val servers = HashSet<String>()

        for ((key, value) in entries) {
            val parsed = PaperServerEntry.decode(value)
            if (parsed == null) {
                logger.warn("Invalid discovery entry for key {}", key)
                continue
            }
            servers.add(parsed.name)
            registerOrUpdate(parsed)
        }

        unregisterMissing(servers)
    }

    private fun unregisterMissing(aliveNames: Set<String>) {
        val iterator = registeredServers.entries.iterator()
        while (iterator.hasNext()) {
            val existing = iterator.next()
            if (!aliveNames.contains(existing.key)) {
                proxyServer.unregisterServer(existing.value)
                iterator.remove()
                logger.info("Unregistered stale server {}", existing.key)
            }
        }
    }

    private fun registerOrUpdate(entry: PaperServerEntry) {
        val name = entry.name
        val address = InetSocketAddress.createUnresolved(entry.host, entry.port)
        val desiredInfo = ServerInfo(name, address)

        val existing = registeredServers[name]
        if (existing != null) {
            if (existing.address != address) {
                proxyServer.unregisterServer(existing)
                proxyServer.registerServer(desiredInfo)
                registeredServers[name] = desiredInfo
                logger.info("Updated discovered server {}", name)
            }
            return
        }

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
            registeredServers[name] = existingInfo
            return
        }

        proxyServer.registerServer(desiredInfo)
        registeredServers[name] = desiredInfo
        logger.info("Registered discovered server {}", name)
    }
}
