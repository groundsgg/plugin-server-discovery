package gg.grounds

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import gg.grounds.discovery.VelocityDiscoveryConfig
import gg.grounds.discovery.VelocityDiscoveryService
import org.slf4j.Logger

@Plugin(id = "plugin-server-discovery", name = "Grounds Server Discovery Plugin")
class GroundsPluginServerDiscovery
@Inject
constructor(private val proxyServer: ProxyServer, private val logger: Logger) {
    private lateinit var discoveryService: VelocityDiscoveryService

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        proxyServer.allServers.forEach { server ->
            logger.info("Removing pre-configured server: {}", server.serverInfo.name)
            proxyServer.unregisterServer(server.serverInfo)
        }

        val config = VelocityDiscoveryConfig.load()

        discoveryService =
            VelocityDiscoveryService(proxyServer, logger, config).also { service ->
                service.start(this)
            }
        logger.info("Velocity server discovery initialized")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        if (this::discoveryService.isInitialized) {
            discoveryService.stop()
        }
    }

    @Subscribe
    fun onPlayerChooseInitialServer(event: PlayerChooseInitialServerEvent) {
        if (event.initialServer.isPresent) {
            return
        }
        discoveryService.pickInitialServer().ifPresent(event::setInitialServer)
    }
}
