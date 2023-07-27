package net.sparkzz.command;

import net.sparkzz.util.Notifiable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Class for Command layout
 *
 * @author Brendon Butler
 */
public abstract class CommandManager extends Notifiable implements TabExecutor {

    /**
     * Registers commands to the server for the plugin
     *
     * @param plugin the plugin to register commands for
     */
    public static void registerCommands(JavaPlugin plugin) {
        // Set command executor(s)
        Optional.ofNullable(plugin.getCommand("shop"))
                .ifPresent(cmd -> cmd.setExecutor(new ShopCommand()));

        Optional.ofNullable(plugin.getCommand("shops"))
                .ifPresent(cmd -> cmd.setExecutor(new InfoCommand()));

        // Set tab completer(s)
        Optional.ofNullable(plugin.getCommand("shop"))
                .ifPresent(cmd -> cmd.setTabCompleter(new ShopCommand()));
    }

    /**
     * TabCompleter for generating suggestions when a player starts typing a command
     *
     * @param sender the sender attempting the command
     * @param command the command to be processed
     * @param label the command label
     * @param args the arguments following the command
     * @return a list of options as strings for the player
     */
    @Override
    public abstract List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args);

    /**
     * The command processing method
     *
     * @param sender the sender attempting the command
     * @param command the command to be processed
     * @param label the command label
     * @param args the arguments following the command
     * @return whether the command was successful or not
     */
    @Override
    public abstract boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args);
}