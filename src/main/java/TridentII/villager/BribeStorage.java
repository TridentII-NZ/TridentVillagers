package TridentII.villager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class BribeStorage {

    private final NamespacedKey cureCountsKey;

    public BribeStorage(JavaPlugin plugin) {
        cureCountsKey = new NamespacedKey(plugin, "cure_counts");
    }

    public int cureCount(Villager villager, UUID playerId) {
        return cureCounts(villager).getOrDefault(playerId, 0);
    }

    public int addCures(Villager villager, UUID playerId, int curesToAdd, int maxCures) {
        Map<UUID, Integer> counts = cureCounts(villager);
        int current = counts.getOrDefault(playerId, 0);
        int updated = Math.min(maxCures, current + Math.max(1, curesToAdd));
        counts.put(playerId, updated);
        saveCureCounts(villager, counts);
        return updated;
    }

    private Map<UUID, Integer> cureCounts(Villager villager) {
        String stored = villager.getPersistentDataContainer().get(cureCountsKey, PersistentDataType.STRING);
        Map<UUID, Integer> counts = new LinkedHashMap<>();
        if (stored == null || stored.isBlank()) {
            return counts;
        }

        for (String entry : stored.split(";")) {
            String[] parts = entry.split("=", 2);
            if (parts.length != 2) {
                continue;
            }

            try {
                counts.put(UUID.fromString(parts[0]), Integer.parseInt(parts[1]));
            } catch (IllegalArgumentException ignored) {
                // Ignore malformed legacy data and keep the rest of the store readable.
            }
        }

        return counts;
    }

    private void saveCureCounts(Villager villager, Map<UUID, Integer> counts) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(';');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }

        villager.getPersistentDataContainer().set(cureCountsKey, PersistentDataType.STRING, builder.toString());
    }
}
