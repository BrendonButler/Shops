package net.sparkzz.command;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.*;

/**
 * Deposit subcommand used for depositing finances into a shop
 *
 * @author Brendon Butler
 */
public class DepositSubCommand extends ISubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        Player player = (Player) sender;
        Store store = InventoryManagementSystem.locateCurrentShop(player);
        double amount = Double.parseDouble(args[1]);

        if (amount < 0) throw new NumberFormatException(String.format("Invalid amount: \"%s\"", args[1]));

        if (!store.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(String.format("%sYou are not the owner of this shop, you cannot perform this command!", RED));
            return true;
        }

        if (store.hasInfiniteFunds()) {
            player.sendMessage(String.format("%sThis store has infinite funds, depositing funds isn't necessary!", RED));
            return true;
        }

        if (amount > Shops.getEconomy().getBalance(player)) {
            player.sendMessage(String.format("%sYou have insufficient funds!", RED));
            return true;
        }

        Shops.getEconomy().withdrawPlayer(player, amount);
        store.addFunds(amount);
        sender.sendMessage(String.format("%sYou have successfully deposited %s%s%s to the shop!", GREEN, GOLD, amount, GREEN));
        return true;
    }
}