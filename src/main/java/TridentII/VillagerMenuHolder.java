package TridentII;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

final class VillagerMenuHolder implements InventoryHolder {

    private final MenuType type;
    private final UUID villagerId;
    private Inventory inventory;

    VillagerMenuHolder(MenuType type, UUID villagerId) {
        this.type = type;
        this.villagerId = villagerId;
    }

    MenuType type() {
        return type;
    }

    UUID villagerId() {
        return villagerId;
    }

    void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    enum MenuType {
        TRADE,
        BRIBE
    }
}
