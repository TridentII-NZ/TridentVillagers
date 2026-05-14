package TridentII.menu;

import TridentII.config.PluginConfig;
import TridentII.format.TextFormatter;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class VillagerMenuService {

    private static final int MENU_SIZE = 27;

    private final PluginConfig config;
    private final TextFormatter formatter;

    public VillagerMenuService(PluginConfig config, TextFormatter formatter) {
        this.config = config;
        this.formatter = formatter;
    }

    public Inventory createTradeMenu(Villager villager) {
        VillagerMenuHolder holder = new VillagerMenuHolder(VillagerMenuHolder.MenuType.TRADE, villager.getUniqueId());
        Inventory inventory = Bukkit.createInventory(holder, MENU_SIZE, component("menus.trade-title"));
        holder.inventory(inventory);
        fillBorder(inventory);
        inventory.setItem(config.integer("menus.trade-button.slot"), configuredItem("menus.trade-button"));
        inventory.setItem(config.integer("menus.bribe-button.slot"), configuredItem("menus.bribe-button"));
        return inventory;
    }

    public Inventory createBribeMenu(Villager villager) {
        VillagerMenuHolder holder = new VillagerMenuHolder(VillagerMenuHolder.MenuType.BRIBE, villager.getUniqueId());
        Inventory inventory = Bukkit.createInventory(holder, MENU_SIZE, component("menus.bribe-title"));
        holder.inventory(inventory);
        fillBorder(inventory);
        inventory.setItem(config.integer("menus.bribe-back-button.slot"), configuredItem("menus.bribe-back-button"));
        inventory.setItem(config.integer("menus.bribe-confirm-button.slot"), configuredItem("menus.bribe-confirm-button"));
        return inventory;
    }

    public int tradeButtonSlot() {
        return config.integer("menus.trade-button.slot");
    }

    public int bribeButtonSlot() {
        return config.integer("menus.bribe-button.slot");
    }

    public int bribeInputSlot() {
        return config.integer("menus.bribe-input-slot");
    }

    public int bribeConfirmSlot() {
        return config.integer("menus.bribe-confirm-button.slot");
    }

    public int bribeBackSlot() {
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
        meta.displayName(component(path + ".name"));
        List<Component> lore = config.textList(path + ".lore").stream()
            .map(this::resolveInline)
            .map(formatter::format)
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

    private Component component(String path) {
        return formatter.format(resolve(path));
    }

    private String resolveInline(String value) {
        return value.replace("%prefix%", config.prefix());
    }
}
