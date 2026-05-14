package TridentII;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

final class MessageService {

    private final JavaPlugin plugin;
    private final PluginConfig config;

    MessageService(JavaPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    void send(CommandSender sender, String messagePath) {
        sender.sendMessage(Component.text(resolve(messagePath)));
    }

    void sendConsole(String messagePath) {
        plugin.getServer().getConsoleSender().sendMessage(Component.text(resolve(messagePath)));
    }

    String resolve(String messagePath) {
        return config.text(messagePath).replace("%prefix%", config.prefix());
    }
}
