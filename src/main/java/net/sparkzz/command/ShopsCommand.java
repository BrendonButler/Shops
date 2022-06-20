package net.sparkzz.command;

import net.sparkzz.shops.Shops;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ShopsCommand extends CommandManager {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(String.format("§l§3Shops v%s", Shops.desc.getVersion()));

        return true;
    }
}