package TridentII.menu;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class VillagerMenuHolder implements InventoryHolder {

    private final MenuType type;
    private final UUID villagerId;
    private Inventory inventory;

    VillagerMenuHolder(MenuType type, UUID villagerId) {
        this.type = type;
        this.villagerId = villagerId;
    }

    public MenuType type() {
        return type;
    }

    public UUID villagerId() {
        return villagerId;
    }

    void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public enum MenuType {
        TRADE,
        BRIBE
    }
}
