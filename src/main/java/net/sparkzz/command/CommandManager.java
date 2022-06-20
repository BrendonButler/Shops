package net.sparkzz.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Class for Command layout
 *
 * @author Brendon Butler
 */
public abstract class CommandManager implements CommandExecutor {

    public static void registerCommands(JavaPlugin plugin) {
        plugin.getCommand("shops").setExecutor(new ShopsCommand());
    }

    @Override
    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);
}