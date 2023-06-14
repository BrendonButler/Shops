package net.sparkzz.command;

import net.sparkzz.shops.Shops;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Shops Command for plugin info/credits
 *
 * @author Brendon Butler
 */
public class InfoCommand extends CommandManager {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("shops.cmd.shops")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        sender.sendMessage(String.format("§l§3Shops v%s", Shops.desc.getVersion()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
}