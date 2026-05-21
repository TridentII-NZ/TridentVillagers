package TridentII.message;

import TridentII.config.PluginConfig;
import TridentII.format.TextFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageService {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final TextFormatter formatter;

    public MessageService(JavaPlugin plugin, PluginConfig config, TextFormatter formatter) {
        this.plugin = plugin;
        this.config = config;
        this.formatter = formatter;
    }

    public void send(CommandSender sender, String messagePath) {
        sender.sendMessage(message(messagePath));
    }

    public void sendConsole(String messagePath) {
        plugin.getServer().getConsoleSender().sendMessage(message(messagePath));
    }

    public String resolve(String messagePath) {
        return config.text(messagePath).replace("%prefix%", config.prefix());
    }

    public String message(String messagePath) {
        return formatter.format(resolve(messagePath));
    }
}
