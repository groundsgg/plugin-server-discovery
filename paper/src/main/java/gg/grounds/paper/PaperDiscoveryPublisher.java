package gg.grounds.paper;

import gg.grounds.discovery.ValkeyClient;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class PaperDiscoveryPublisher {
    private final JavaPlugin plugin;
    private final PaperDiscoveryConfig config;
    private final ValkeyClient client;
    private final String entryKey;
    private BukkitTask heartbeatTask;

    public PaperDiscoveryPublisher(JavaPlugin plugin, PaperDiscoveryConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.client = new ValkeyClient(config.valkeyConfig());
        this.entryKey = config.discoveryKeys().paperServerKey(config.baseEntry().name());
    }

    public void start() {
        updateEntry();
        heartbeatTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::updateEntry,
                20L,
                20L
        );
    }

    public void stop() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
        }
        try {
            client.delete(entryKey);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove discovery entry: " + e.getMessage());
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to close Valkey client: " + e.getMessage());
            }
        }
    }

    private void updateEntry() {
        try {
            client.setWithTtl(entryKey, config.baseEntry().encode(), 3L);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update discovery entry: " + e.getMessage());
        }
    }
}
