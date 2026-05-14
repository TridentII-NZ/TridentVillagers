package TridentII;

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
        messageService = new MessageService(this, pluginConfig);
        VillagerTradeService tradeService = new VillagerTradeService(pluginConfig);
        VillagerMenuService menuService = new VillagerMenuService(this, pluginConfig);
        menuListener = new VillagerMenuListener(this, pluginConfig, messageService, menuService, tradeService);
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
