package TridentII.villager;

import TridentII.config.PluginConfig;
import TridentII.message.MessageService;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class BedHighlightService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final MessageService messages;

    public BedHighlightService(JavaPlugin plugin, PluginConfig config, MessageService messages) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
    }

    public void highlight(Player player, Villager villager) {
        if (!config.bedHighlight()) {
            return;
        }

        Location bed = villager.getMemory(MemoryKey.HOME);
        if (bed == null) {
            messages.send(player, "messages.bed-no-bed");
            return;
        }

        messages.send(player, "messages.bed-highlighted");
        Location center = bed.clone().add(0.5, 0.5, 0.5);

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count++ >= 25) {
                    cancel();
                    return;
                }
                player.spawnParticle(Particle.HAPPY_VILLAGER, center, 5, 0.4, 0.3, 0.4, 0);
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }
}
