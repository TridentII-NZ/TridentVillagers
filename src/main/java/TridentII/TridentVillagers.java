package TridentII;

import TridentII.command.TridentVillagersCommand;
import TridentII.config.PluginConfig;
import TridentII.format.TextFormatter;
import TridentII.message.MessageService;
import TridentII.menu.VillagerMenuService;
import TridentII.villager.BedHighlightService;
import TridentII.villager.BribeStorage;
import TridentII.villager.RestockService;
import TridentII.villager.VillagerMenuListener;
import TridentII.villager.VillagerTradeService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class TridentVillagers extends JavaPlugin {

    private PluginConfig pluginConfig;
    private MessageService messageService;
    private VillagerMenuListener menuListener;
    private RestockService restockService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadServices();
        registerCommand();
        registerListeners();
        restockService.start();
        messageService.sendConsole("messages.startup");
    }

    @Override
    public void onDisable() {
        if (messageService != null) {
            messageService.sendConsole("messages.shutdown");
        }
        if (restockService != null) {
            restockService.stop();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        pluginConfig.reload();
        restockService.restart();
    }

    private void loadServices() {
        pluginConfig = new PluginConfig(this);
        TextFormatter formatter = new TextFormatter();
        BribeStorage bribeStorage = new BribeStorage(this);
        messageService = new MessageService(this, pluginConfig, formatter);
        VillagerTradeService tradeService = new VillagerTradeService(pluginConfig, bribeStorage);
        VillagerMenuService menuService = new VillagerMenuService(pluginConfig, formatter);
        BedHighlightService bedHighlightService = new BedHighlightService(this, pluginConfig, messageService);
        menuListener = new VillagerMenuListener(this, pluginConfig, messageService, menuService, tradeService, bedHighlightService);
        restockService = new RestockService(this, pluginConfig, tradeService);
    }

    private void registerCommand() {
        PluginCommand command = getCommand("tridentvillagers");
        if (command == null) {
            getLogger().warning("Command tridentvillagers is missing from plugin.yml.");
            return;
        }

        TridentVillagersCommand executor = new TridentVillagersCommand(this, messageService);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(menuListener, this);
    }
}
