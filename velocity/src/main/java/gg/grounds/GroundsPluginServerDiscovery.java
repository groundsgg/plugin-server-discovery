package gg.grounds;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.grounds.velocity.VelocityDiscoveryConfig;
import gg.grounds.velocity.VelocityDiscoveryService;
import org.slf4j.Logger;

@Plugin(id = "plugin-server-discovery", name = "Grounds Server Discovery Plugin")
public final class GroundsPluginServerDiscovery {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private VelocityDiscoveryService discoveryService;

    @Inject
    public GroundsPluginServerDiscovery(
            ProxyServer proxyServer,
            Logger logger
    ) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        proxyServer.getAllServers().forEach(server -> {
            logger.info("Removing pre-configured server: {}", server.getServerInfo().getName());
            proxyServer.unregisterServer(server.getServerInfo());
        });

        VelocityDiscoveryConfig config = VelocityDiscoveryConfig.load();

        discoveryService = new VelocityDiscoveryService(proxyServer, logger, config);
        discoveryService.start(this);
        logger.info("Velocity server discovery initialized");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (discoveryService != null) {
            discoveryService.stop();
        }
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        if (discoveryService == null || event.getInitialServer().isPresent()) {
            return;
        }
        discoveryService.pickInitialServer().ifPresent(event::setInitialServer);
    }
}
