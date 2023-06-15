package net.sparkzz.command;

import net.sparkzz.shops.Store;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GREEN;

/**
 * Create subcommand used for creating a shop
 *
 * @author Brendon Butler
 */
public class CreateSubCommand implements ISubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        // TODO: new permission to limit a player to a number of shops (shops.create.<quantity>)

        Store store = new Store(args[1], ((Player) sender).getUniqueId());
        sender.sendMessage(String.format("%sYou have successfully created %s%s%s!", GREEN, GOLD, store.getName(), GREEN));
        return true;
    }
}