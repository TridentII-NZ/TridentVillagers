package TridentII.command;

import TridentII.TridentVillagers;
import TridentII.message.MessageService;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TridentVillagersCommand implements TabExecutor {

    private static final String RELOAD_ARGUMENT = "reload";

    private final TridentVillagers plugin;
    private final MessageService messages;

    public TridentVillagersCommand(TridentVillagers plugin, MessageService messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase(RELOAD_ARGUMENT)) {
            if (!sender.hasPermission("tridentvillagers.reload")) {
                messages.send(sender, "messages.no-permission");
                return true;
            }

            plugin.reloadPlugin();
            messages.send(sender, "messages.reload-success");
            return true;
        }

        messages.send(sender, "messages.unknown-command");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1 && sender.hasPermission("tridentvillagers.reload")) {
            return List.of(RELOAD_ARGUMENT).stream()
                .filter(option -> option.startsWith(args[0].toLowerCase(Locale.ROOT)))
                .toList();
        }

        return List.of();
    }
}
