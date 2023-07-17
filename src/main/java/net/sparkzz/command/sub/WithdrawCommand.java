package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
import net.sparkzz.util.Notifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.sparkzz.util.Notifier.CipherKey.*;

/**
 * Withdraw subcommand used for withdrawing funds from a shop
 *
 * @author Brendon Butler
 */
public class WithdrawCommand extends SubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        resetAttributes();
        setArgsAsAttributes(args);
        Player player = (Player) setAttribute("sender", sender);
        Store store = (Store) setAttribute("store", InventoryManagementSystem.locateCurrentShop(player));
        double amount = (Double) setAttribute("amount", (args[1].equalsIgnoreCase("all")) ? store.getBalance() : Double.parseDouble(args[1]));

        if (amount < 0) throw new NumberFormatException(String.format("Invalid amount: \"%s\"", args[1]));

        if (!store.getOwner().equals(player.getUniqueId())) {
            Notifier.process(sender, NOT_OWNER, getAttributes());
            return true;
        }

        if (amount > store.getBalance()) {
            Notifier.process(sender, INSUFFICIENT_FUNDS_STORE, getAttributes());
            return true;
        }

        store.removeFunds(amount);
        Shops.getEconomy().depositPlayer(player, amount);
        Notifier.process(sender, WITHDRAW_SUCCESS, getAttributes());
        return true;
    }
}