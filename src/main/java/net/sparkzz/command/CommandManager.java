package net.sparkzz.command;

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
public abstract class CommandManager implements TabExecutor {

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

    @Override
    public abstract List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args);

    @Override
    public abstract boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args);
}