package net.sparkzz.shops.command.sub;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;

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
        BigDecimal amount = (BigDecimal) setAttribute("amount", (args[1].equalsIgnoreCase("all")) ? (store == null) ? BigDecimal.ZERO : store.getBalance() : BigDecimal.valueOf(Double.parseDouble(args[1])));

        if (store == null) {
            Notifier.process(player, NO_STORE_FOUND, getAttributes());
            return true;
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException(String.format("Invalid amount: \"%s\"", args[1]));

        if (!store.getOwner().equals(player.getUniqueId())) {
            Notifier.process(sender, NOT_OWNER, getAttributes());
            return true;
        }

        if (amount.compareTo(store.getBalance()) > 0) {
            Notifier.process(sender, INSUFFICIENT_FUNDS_STORE, getAttributes());
            return true;
        }

        store.removeFunds(amount);
        Shops.getEconomy().depositPlayer(player, amount.doubleValue());
        Notifier.process(sender, WITHDRAW_SUCCESS, getAttributes());
        return true;
    }
}