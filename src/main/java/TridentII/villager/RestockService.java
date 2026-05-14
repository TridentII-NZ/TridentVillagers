package TridentII.villager;

import TridentII.config.PluginConfig;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class RestockService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final VillagerTradeService trades;
    private BukkitTask task;

    public RestockService(JavaPlugin plugin, PluginConfig config, VillagerTradeService trades) {
        this.plugin = plugin;
        this.config = config;
        this.trades = trades;
    }

    public void start() {
        stop();
        if (!config.alwaysRestock()) {
            return;
        }

        long interval = config.restockIntervalTicks();
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::restockLoadedVillagers, interval, interval);
    }

    public void restart() {
        start();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void restockLoadedVillagers() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntitiesByClass(Villager.class)) {
                trades.restock((Villager) entity);
            }
        }
    }
}
