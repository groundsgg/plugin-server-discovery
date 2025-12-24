package gg.grounds.velocity;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import gg.grounds.discovery.PaperServerEntry;
import gg.grounds.discovery.ValkeyClient;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class VelocityDiscoveryService {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final VelocityDiscoveryConfig config;
    private final ValkeyClient client;
    private final Map<String, ServerInfo> registeredServers = new ConcurrentHashMap<>();
    private ScheduledTask pollTask;

    public VelocityDiscoveryService(
            ProxyServer proxyServer,
            Logger logger,
            VelocityDiscoveryConfig config
    ) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.config = config;
        this.client = new ValkeyClient(config.valkeyConfig());
    }

    public void start(Object plugin) {
        refreshServers();
        pollTask = proxyServer.getScheduler()
                .buildTask(plugin, this::refreshServers)
                .repeat(5, TimeUnit.SECONDS)
                .schedule();
    }

    public void stop() {
        if (pollTask != null) {
            pollTask.cancel();
        }
        for (ServerInfo info : registeredServers.values()) {
            proxyServer.unregisterServer(info);
        }
        registeredServers.clear();
        try {
            client.close();
        } catch (Exception e) {
            logger.warn("Failed to close Valkey client: {}", e.getMessage());
        }
    }

    public Optional<RegisteredServer> pickInitialServer() {
        for (ServerInfo info : registeredServers.values()) {
            return proxyServer.getServer(info.getName());
        }
        return Optional.empty();
    }

    private void refreshServers() {
        Map<String, String> entries;
        try {
            entries = client.scanValues(config.discoveryKeys().paperServerPattern());
        } catch (Exception e) {
            logger.warn("Failed to scan Valkey for servers: {}", e.getMessage());
            return;
        }

        Set<String> servers = new HashSet<>();

        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Optional<PaperServerEntry> parsed = PaperServerEntry.decode(entry.getValue());
            if (parsed.isEmpty()) {
                logger.warn("Invalid discovery entry for key {}", entry.getKey());
                continue;
            }
            PaperServerEntry server = parsed.get();
            servers.add(server.name());
            registerOrUpdate(server);
        }

        unregisterMissing(servers);
    }

    private void unregisterMissing(Set<String> aliveNames) {
        Iterator<Map.Entry<String, ServerInfo>> iterator = registeredServers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServerInfo> existing = iterator.next();
            if (!aliveNames.contains(existing.getKey())) {
                proxyServer.unregisterServer(existing.getValue());
                iterator.remove();
                logger.info("Unregistered stale server {}", existing.getKey());
            }
        }
    }

    private void registerOrUpdate(PaperServerEntry entry) {
        String name = entry.name();
        InetSocketAddress address = InetSocketAddress.createUnresolved(entry.host(), entry.port());
        ServerInfo desiredInfo = new ServerInfo(name, address);

        ServerInfo existing = registeredServers.get(name);
        if (existing != null) {
            if (!existing.getAddress().equals(address)) {
                proxyServer.unregisterServer(existing);
                proxyServer.registerServer(desiredInfo);
                registeredServers.put(name, desiredInfo);
                logger.info("Updated discovered server {}", name);
            }
            return;
        }

        Optional<RegisteredServer> proxyExisting = proxyServer.getServer(name);
        if (proxyExisting.isPresent()) {
            ServerInfo existingInfo = proxyExisting.get().getServerInfo();
            if (!existingInfo.getAddress().equals(address)) {
                logger.warn("Skipping discovered server {} because it conflicts with an existing registration", name);
                return;
            }
            registeredServers.put(name, existingInfo);
            return;
        }

        proxyServer.registerServer(desiredInfo);
        registeredServers.put(name, desiredInfo);
        logger.info("Registered discovered server {}", name);
    }
}
