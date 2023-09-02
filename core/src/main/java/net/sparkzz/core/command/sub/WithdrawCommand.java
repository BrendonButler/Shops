package net.sparkzz.core.command.sub;

import net.sparkzz.core.command.SubCommand;
import net.sparkzz.core.shops.Shops;
import net.sparkzz.core.shops.Store;
import net.sparkzz.core.util.InventoryManagementSystem;
import net.sparkzz.core.util.Notifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.sparkzz.core.util.Notifier.CipherKey.*;

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
        Store store = (Store) setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));
        double amount = (Double) setAttribute("amount", (args[1].equalsIgnoreCase("all")) ? (store == null) ? 0 : store.getBalance() : Double.parseDouble(args[1]));

        if (store == null) {
            Notifier.process(player, NO_STORE_FOUND, getAttributes());
            return true;
        }

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