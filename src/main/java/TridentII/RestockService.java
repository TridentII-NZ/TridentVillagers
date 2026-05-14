package TridentII;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

final class RestockService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final VillagerTradeService trades;
    private BukkitTask task;

    RestockService(JavaPlugin plugin, PluginConfig config, VillagerTradeService trades) {
        this.plugin = plugin;
        this.config = config;
        this.trades = trades;
    }

    void start() {
        stop();
        if (!config.bool("features.always-restock")) {
            return;
        }

        long interval = Math.max(20L, config.integer("restock.interval-ticks"));
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::restockLoadedVillagers, interval, interval);
    }

    void restart() {
        start();
    }

    void stop() {
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
