package net.sparkzz.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Class for Command layout
 *
 * @author Brendon Butler
 */
public abstract class CommandManager implements TabExecutor {

    public static void registerCommands(JavaPlugin plugin) {
        // Set command executor(s)
        plugin.getCommand("shop").setExecutor(new ShopCommand());
        plugin.getCommand("shops").setExecutor(new InfoCommand());

        // Set tab completer(s)
        plugin.getCommand("shop").setTabCompleter(new ShopCommand());
    }

    @Override
    public abstract List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args);

    @Override
    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);
}