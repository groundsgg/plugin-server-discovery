package gg.grounds

import gg.grounds.discovery.PaperDiscoveryConfig
import gg.grounds.discovery.PaperDiscoveryPublisher
import org.bukkit.plugin.java.JavaPlugin

class GroundsPluginServerDiscovery : JavaPlugin() {
    private var publisher: PaperDiscoveryPublisher? = null

    override fun onEnable() {
        val config = PaperDiscoveryConfig.load()

        publisher = PaperDiscoveryPublisher(this, config)
        publisher?.start()

        logger.info("Paper server discovery initialized")
    }

    override fun onDisable() {
        publisher?.stop()
    }
}
