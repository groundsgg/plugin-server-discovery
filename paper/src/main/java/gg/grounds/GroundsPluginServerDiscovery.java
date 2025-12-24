package gg.grounds;

import gg.grounds.paper.PaperDiscoveryConfig;
import gg.grounds.paper.PaperDiscoveryPublisher;
import org.bukkit.plugin.java.JavaPlugin;

public final class GroundsPluginServerDiscovery extends JavaPlugin {
    private PaperDiscoveryPublisher publisher;

    @Override
    public void onEnable() {
        PaperDiscoveryConfig config = PaperDiscoveryConfig.load(getLogger());

        publisher = new PaperDiscoveryPublisher(this, config);
        publisher.start();

        getLogger().info("Paper server discovery initialized");
    }

    @Override
    public void onDisable() {
        if (publisher != null) {
            publisher.stop();
        }
    }
}
