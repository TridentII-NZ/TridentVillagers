package TridentII.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConfig {

    private final JavaPlugin plugin;
    private List<BribeOffer> bribeOffers = List.of();
    private boolean alwaysRestock;
    private boolean bribesEnabled;
    private boolean resetUses;
    private boolean resetDemand;
    private long restockIntervalTicks;
    private int cureReputationValue;
    private int maxCuresPerPlayer;
    private int priceDiscountPerCure;
    private int maxPriceDiscount;

    public PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration config = plugin.getConfig();
        bribeOffers = loadBribeOffers(config);
        alwaysRestock = config.getBoolean("features.always-restock");
        bribesEnabled = config.getBoolean("features.bribes", true);
        resetUses = config.getBoolean("restock.reset-uses");
        resetDemand = config.getBoolean("restock.reset-demand");
        restockIntervalTicks = Math.max(20L, config.getLong("restock.interval-ticks"));
        cureReputationValue = Math.max(1, config.getInt("bribes.curing.cure-reputation-value", 20));
        maxCuresPerPlayer = Math.max(1, config.getInt("bribes.curing.max-cures-per-player", 5));
        priceDiscountPerCure = Math.max(1, config.getInt("bribes.curing.price-discount-per-cure", 20));
        maxPriceDiscount = Math.max(1, config.getInt("bribes.curing.max-price-discount", 100));
    }

    public String prefix() {
        return text("prefix");
    }

    public String text(String path) {
        return plugin.getConfig().getString(path, "");
    }

    public List<String> textList(String path) {
        return plugin.getConfig().getStringList(path);
    }

    public boolean alwaysRestock() {
        return alwaysRestock;
    }

    public boolean bribesEnabled() {
        return bribesEnabled;
    }

    public boolean resetUses() {
        return resetUses;
    }

    public boolean resetDemand() {
        return resetDemand;
    }

    public long restockIntervalTicks() {
        return restockIntervalTicks;
    }

    public int cureReputationValue() {
        return cureReputationValue;
    }

    public int maxCuresPerPlayer() {
        return maxCuresPerPlayer;
    }

    public int priceDiscountPerCure() {
        return priceDiscountPerCure;
    }

    public int maxPriceDiscount() {
        return maxPriceDiscount;
    }

    public int integer(String path) {
        return plugin.getConfig().getInt(path);
    }

    public Material material(String path, Material fallback) {
        String configured = text(path);
        Material material = Material.matchMaterial(configured);
        return material == null ? fallback : material;
    }

    public List<BribeOffer> bribeOffers() {
        return bribeOffers;
    }

    private List<BribeOffer> loadBribeOffers(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("bribes.accepted-items");
        if (section == null) {
            return Collections.emptyList();
        }

        int defaultCures = Math.max(1, config.getInt("bribes.defaults.cure-count", 1));
        List<BribeOffer> offers = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            String base = "bribes.accepted-items." + key;
            Material material = Material.matchMaterial(config.getString(base + ".material", ""));
            if (material == null) {
                plugin.getLogger().warning("Invalid bribe material at " + base + ".material");
                continue;
            }

            int amount = Math.max(1, config.getInt(base + ".amount", 1));
            int cureCount = Math.max(1, config.getInt(base + ".cure-count", defaultCures));
            offers.add(new BribeOffer(key.toLowerCase(Locale.ROOT), material, amount, cureCount));
        }

        return List.copyOf(offers);
    }

    public record BribeOffer(String key, Material material, int amount, int cureCount) {

        public boolean accepts(ItemStack itemStack) {
            return itemStack != null && itemStack.getType() == material && itemStack.getAmount() >= amount;
        }
    }
}
