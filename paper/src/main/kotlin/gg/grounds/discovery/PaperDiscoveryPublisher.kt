package gg.grounds.discovery

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class PaperDiscoveryPublisher(
    private val plugin: JavaPlugin,
    private val config: PaperDiscoveryConfig,
) {
    private val client = ValkeyClient(config.valkeyConfig)
    private val entryHashKey = DiscoveryKeys.paperServersHashKey()
    private var heartbeatTask: BukkitTask? = null

    fun start() {
        updateEntry()
        heartbeatTask =
            plugin.server.scheduler.runTaskTimerAsynchronously(plugin, this::updateEntry, 20L, 20L)
    }

    fun stop() {
        heartbeatTask?.cancel()
        try {
            client.hdel(entryHashKey, config.baseEntry.name)
        } catch (e: Exception) {
            plugin.logger.warning("Failed to remove discovery entry: ${e.message}")
        } finally {
            try {
                client.close()
            } catch (e: Exception) {
                plugin.logger.warning("Failed to close Valkey client: ${e.message}")
            }
        }
    }

    private fun updateEntry() {
        try {
            val entry = config.baseEntry.copy(lastSeenMillis = System.currentTimeMillis())
            client.hset(entryHashKey, entry.name, entry.encode())
        } catch (e: Exception) {
            plugin.logger.warning("Failed to update discovery entry: ${e.message}")
        }
    }
}
