package net.sparkzz.command;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.*;

/**
 * Withdraw subcommand used for withdrawing funds from a shop
 *
 * @author Brendon Butler
 */
public class WithdrawSubCommand extends ISubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        Player player = (Player) sender;
        Store store = InventoryManagementSystem.locateCurrentShop(player);
        double amount = (args[1].equalsIgnoreCase("all")) ? store.getBalance() : Double.parseDouble(args[1]);

        if (amount < 0) throw new NumberFormatException(String.format("Invalid amount: \"%s\"", args[1]));

        if (!store.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(String.format("%sYou are not the owner of this shop, you cannot perform this command!", RED));
            return true;
        }

        if (amount > store.getBalance()) {
            player.sendMessage(String.format("%sThe Store has insufficient funds!", RED));
            return true;
        }

        store.removeFunds(amount);
        Shops.getEconomy().depositPlayer(player, amount);
        sender.sendMessage(String.format("%sYou have successfully withdrawn %s%s%s from the shop!", GREEN, GOLD, amount, GREEN));
        return true;
    }
}