package TridentII;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

final class VillagerMenuService {

    private static final int MENU_SIZE = 27;

    private final JavaPlugin plugin;
    private final PluginConfig config;

    VillagerMenuService(JavaPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    Inventory createTradeMenu(Villager villager) {
        VillagerMenuHolder holder = new VillagerMenuHolder(VillagerMenuHolder.MenuType.TRADE, villager.getUniqueId());
        Inventory inventory = Bukkit.createInventory(holder, MENU_SIZE, Component.text(resolve("menus.trade-title")));
        holder.inventory(inventory);
        fillBorder(inventory);
        inventory.setItem(config.integer("menus.trade-button.slot"), configuredItem("menus.trade-button"));
        inventory.setItem(config.integer("menus.bribe-button.slot"), configuredItem("menus.bribe-button"));
        return inventory;
    }

    Inventory createBribeMenu(Villager villager) {
        VillagerMenuHolder holder = new VillagerMenuHolder(VillagerMenuHolder.MenuType.BRIBE, villager.getUniqueId());
        Inventory inventory = Bukkit.createInventory(holder, MENU_SIZE, Component.text(resolve("menus.bribe-title")));
        holder.inventory(inventory);
        fillBorder(inventory);
        inventory.setItem(config.integer("menus.bribe-back-button.slot"), configuredItem("menus.bribe-back-button"));
        inventory.setItem(config.integer("menus.bribe-confirm-button.slot"), configuredItem("menus.bribe-confirm-button"));
        return inventory;
    }

    int tradeButtonSlot() {
        return config.integer("menus.trade-button.slot");
    }

    int bribeButtonSlot() {
        return config.integer("menus.bribe-button.slot");
    }

    int bribeInputSlot() {
        return config.integer("menus.bribe-input-slot");
    }

    int bribeConfirmSlot() {
        return config.integer("menus.bribe-confirm-button.slot");
    }

    int bribeBackSlot() {
        return config.integer("menus.bribe-back-button.slot");
    }

    private void fillBorder(Inventory inventory) {
        ItemStack filler = configuredItem("menus.filler");
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot < 9 || slot > 17 || slot % 9 == 0 || slot % 9 == 8) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private ItemStack configuredItem(String path) {
        Material material = config.material(path + ".material", Material.STONE);
        ItemStack itemStack = ItemStack.of(material);
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.text(resolve(path + ".name")));
        List<Component> lore = config.textList(path + ".lore").stream()
            .map(this::resolveInline)
            .<Component>map(Component::text)
            .toList();
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private String resolve(String path) {
        return resolveInline(config.text(path));
    }

    private String resolveInline(String value) {
        return value.replace("%prefix%", config.prefix());
    }
}
