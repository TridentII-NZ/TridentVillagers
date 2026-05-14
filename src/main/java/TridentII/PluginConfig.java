package TridentII;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

final class PluginConfig {

    private final JavaPlugin plugin;
    private List<BribeOffer> bribeOffers = List.of();

    PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    void reload() {
        bribeOffers = loadBribeOffers(plugin.getConfig());
    }

    String prefix() {
        return text("prefix");
    }

    String text(String path) {
        return plugin.getConfig().getString(path, "");
    }

    List<String> textList(String path) {
        return plugin.getConfig().getStringList(path);
    }

    boolean bool(String path) {
        return plugin.getConfig().getBoolean(path);
    }

    int integer(String path) {
        return plugin.getConfig().getInt(path);
    }

    Material material(String path, Material fallback) {
        String configured = text(path);
        Material material = Material.matchMaterial(configured);
        return material == null ? fallback : material;
    }

    List<BribeOffer> bribeOffers() {
        return bribeOffers;
    }

    private List<BribeOffer> loadBribeOffers(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("bribes.accepted-items");
        if (section == null) {
            return Collections.emptyList();
        }

        int defaultReputation = config.getInt("bribes.defaults.major-positive-reputation");
        int defaultDiscount = config.getInt("bribes.defaults.special-price-discount");
        List<BribeOffer> offers = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            String base = "bribes.accepted-items." + key;
            Material material = Material.matchMaterial(config.getString(base + ".material", ""));
            if (material == null) {
                plugin.getLogger().warning("Invalid bribe material at " + base + ".material");
                continue;
            }

            int amount = Math.max(1, config.getInt(base + ".amount", 1));
            int reputation = config.getInt(base + ".major-positive-reputation", defaultReputation);
            int discount = config.getInt(base + ".special-price-discount", defaultDiscount);
            offers.add(new BribeOffer(key.toLowerCase(Locale.ROOT), material, amount, reputation, discount));
        }

        return List.copyOf(offers);
    }

    record BribeOffer(String key, Material material, int amount, int majorPositiveReputation, int specialPriceDiscount) {

        boolean accepts(ItemStack itemStack) {
            return itemStack != null && itemStack.getType() == material && itemStack.getAmount() >= amount;
        }
    }
}
