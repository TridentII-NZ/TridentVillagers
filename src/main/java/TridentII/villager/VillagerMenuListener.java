package TridentII.villager;

import TridentII.config.PluginConfig;
import TridentII.menu.VillagerMenuHolder;
import TridentII.menu.VillagerMenuService;
import TridentII.message.MessageService;
import TridentII.villager.BedHighlightService;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.plugin.java.JavaPlugin;

public final class VillagerMenuListener implements Listener {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final MessageService messages;
    private final VillagerMenuService menus;
    private final VillagerTradeService trades;
    private final BedHighlightService bedHighlight;

    public VillagerMenuListener(
        JavaPlugin plugin,
        PluginConfig config,
        MessageService messages,
        VillagerMenuService menus,
        VillagerTradeService trades,
        BedHighlightService bedHighlight
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.menus = menus;
        this.trades = trades;
        this.bedHighlight = bedHighlight;
    }

    @EventHandler
    public void onVillagerInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !(event.getRightClicked() instanceof Villager villager)) {
            return;
        }

        Player player = event.getPlayer();

        if (player.isSneaking()) {
            event.setCancelled(true);
            bedHighlight.highlight(player, villager);
            return;
        }

        trades.applyStoredCures(player, villager);
        if (config.villagerLeads() && player.getInventory().getItemInMainHand().getType() == Material.LEAD) {
            leashVillager(event, player, villager);
            return;
        }

        event.setCancelled(true);
        player.openInventory(menus.createTradeMenu(villager));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof VillagerMenuHolder holder) || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (holder.type() == VillagerMenuHolder.MenuType.TRADE) {
            handleTradeMenuClick(event, player, holder);
            return;
        }

        handleBribeMenuClick(event, player, holder);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof VillagerMenuHolder holder)) {
            return;
        }

        if (holder.type() == VillagerMenuHolder.MenuType.TRADE) {
            event.setCancelled(true);
            return;
        }

        int inputSlot = menus.bribeInputSlot();
        boolean touchesProtectedSlot = event.getRawSlots().stream()
            .anyMatch(slot -> slot < event.getInventory().getSize() && slot != inputSlot);
        if (touchesProtectedSlot) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof VillagerMenuHolder holder)
            || holder.type() != VillagerMenuHolder.MenuType.BRIBE) {
            return;
        }

        ItemStack bribe = event.getInventory().getItem(menus.bribeInputSlot());
        if (bribe != null && bribe.getType() != Material.AIR && bribe.getAmount() > 0 && event.getPlayer() instanceof Player player) {
            player.getInventory().addItem(bribe).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
    }

    @EventHandler
    public void onPlayerTrade(PlayerTradeEvent event) {
        if (!(event.getVillager() instanceof Villager villager)) {
            return;
        }

        trades.applyStoredCures(event.getPlayer(), villager);
        if (config.alwaysRestock()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> trades.restock(villager));
        }
    }

    @EventHandler
    public void onVillagerFoodPickup(EntityPickupItemEvent event) {
        if (!config.alwaysWilling() || !(event.getEntity() instanceof Villager villager)) {
            return;
        }

        Material type = event.getItem().getItemStack().getType();
        if (type == Material.BREAD || type == Material.CARROT
                || type == Material.POTATO || type == Material.BEETROOT) {
            villager.setBreed(true);
        }
    }

    private void handleTradeMenuClick(InventoryClickEvent event, Player player, VillagerMenuHolder holder) {
        event.setCancelled(true);
        Villager villager = findVillager(holder, player);
        if (villager == null) {
            messages.send(player, "messages.villager-missing");
            player.closeInventory();
            return;
        }

        if (event.getRawSlot() == menus.tradeButtonSlot()) {
            trades.restock(villager);
            trades.applyStoredCures(player, villager);
            MenuType.MERCHANT.builder()
                .merchant(villager)
                .checkReachable(true)
                .build(player)
                .open();
        } else if (event.getRawSlot() == menus.bribeButtonSlot() && config.bribesEnabled()) {
            player.openInventory(menus.createBribeMenu(villager));
        }
    }

    private void handleBribeMenuClick(InventoryClickEvent event, Player player, VillagerMenuHolder holder) {
        int rawSlot = event.getRawSlot();
        int topSize = event.getInventory().getSize();
        boolean clickedTopMenu = rawSlot >= 0 && rawSlot < topSize;

        if (event.isShiftClick()) {
            handleBribeShiftClick(event, player, clickedTopMenu);
            return;
        }

        if (clickedTopMenu && rawSlot != menus.bribeInputSlot()) {
            event.setCancelled(true);
        }

        Villager villager = findVillager(holder, player);
        if (villager == null) {
            messages.send(player, "messages.villager-missing");
            player.closeInventory();
            return;
        }

        if (rawSlot == menus.bribeBackSlot()) {
            player.openInventory(menus.createTradeMenu(villager));
            return;
        }

        if (rawSlot == menus.bribeConfirmSlot()) {
            ItemStack offeredItem = event.getInventory().getItem(menus.bribeInputSlot());
            if (trades.applyBribe(player, villager, offeredItem)) {
                messages.send(player, "messages.bribe-accepted");
                player.openInventory(menus.createTradeMenu(villager));
            } else {
                messages.send(player, "messages.bribe-rejected");
            }
        }
    }

    private void handleBribeShiftClick(InventoryClickEvent event, Player player, boolean clickedTopMenu) {
        event.setCancelled(true);

        if (clickedTopMenu) {
            if (event.getRawSlot() == menus.bribeInputSlot()) {
                moveInputStackToPlayer(event, player);
            }
            return;
        }

        movePlayerStackToInput(event);
    }

    private void movePlayerStackToInput(InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getAmount() <= 0) {
            return;
        }

        Inventory menu = event.getInventory();
        int inputSlot = menus.bribeInputSlot();
        ItemStack input = menu.getItem(inputSlot);

        if (input == null || input.getType() == Material.AIR || input.getAmount() <= 0) {
            menu.setItem(inputSlot, clicked.clone());
            event.setCurrentItem(null);
            return;
        }

        if (!input.isSimilar(clicked)) {
            return;
        }

        int space = input.getMaxStackSize() - input.getAmount();
        if (space <= 0) {
            return;
        }

        int transferAmount = Math.min(space, clicked.getAmount());
        input.setAmount(input.getAmount() + transferAmount);
        clicked.setAmount(clicked.getAmount() - transferAmount);

        if (clicked.getAmount() <= 0) {
            event.setCurrentItem(null);
        }
    }

    private void moveInputStackToPlayer(InventoryClickEvent event, Player player) {
        ItemStack input = event.getInventory().getItem(menus.bribeInputSlot());
        if (input == null || input.getType() == Material.AIR || input.getAmount() <= 0) {
            return;
        }

        event.getInventory().setItem(menus.bribeInputSlot(), null);
        player.getInventory().addItem(input).values().forEach(leftover -> event.getInventory().setItem(menus.bribeInputSlot(), leftover));
    }

    private void leashVillager(PlayerInteractEntityEvent event, Player player, Villager villager) {
        event.setCancelled(true);
        if (villager.isLeashed()) {
            return;
        }

        if (villager.setLeashHolder(player)) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                ItemStack lead = player.getInventory().getItemInMainHand();
                lead.setAmount(lead.getAmount() - 1);
            }
            messages.send(player, "messages.villager-leashed");
        }
    }

    private Villager findVillager(VillagerMenuHolder holder, Player player) {
        Entity entity = plugin.getServer().getEntity(holder.villagerId());
        if (entity instanceof Villager villager && villager.getWorld().equals(player.getWorld())) {
            return villager;
        }
        return null;
    }
}
