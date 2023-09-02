package net.sparkzz.core.command;

import net.sparkzz.core.shops.Shops;
import net.sparkzz.core.util.Notifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Shops Command for plugin info/credits
 *
 * @author Brendon Butler
 */
public class InfoCommand extends CommandManager {

    /**
     * The base command for the plugin to provide plugin details
     *
     * @param sender the sender attempting the command
     * @param command the command to be processed
     * @param label the command label
     * @param args the arguments following the command
     * @return whether the command was successful
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("shops.cmd.shops")) {
            Notifier.process(sender, Notifier.CipherKey.NO_PERMS_CMD, null);
            return true;
        }

        sender.sendMessage(String.format("§l§3Shops v%s", Shops.getDesc().getVersion()));
        return true;
    }

    /**
     * TabCompleter for generating suggestions when a player starts typing the /shops command
     *
     * @param sender the sender attempting the command
     * @param command the command to be processed
     * @param label the command label
     * @param args the arguments following the command
     * @return null as there are no options for this command
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        return Collections.emptyList();
    }
}