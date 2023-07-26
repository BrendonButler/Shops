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
 * Deposit subcommand used for depositing finances into a shop
 *
 * @author Brendon Butler
 */
public class DepositCommand extends SubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        resetAttributes();
        setArgsAsAttributes(args);
        Player player = (Player) setAttribute("sender", sender);
        Store store = InventoryManagementSystem.locateCurrentShop(player);
        setAttribute("store", store.getName());
        double amount = (Double) setAttribute("amount", Double.parseDouble(args[1]));

        if (amount < 0) throw new NumberFormatException(String.format("Invalid amount: \"%s\"", args[1]));

        if (!store.getOwner().equals(player.getUniqueId())) {
            Notifier.process(sender, NOT_OWNER, getAttributes());
            return true;
        }

        if (store.hasInfiniteFunds()) {
            Notifier.process(sender, DEPOSIT_INF_FUNDS, getAttributes());
            return true;
        }

        if (amount > Shops.getEconomy().getBalance(player)) {
            Notifier.process(sender, INSUFFICIENT_FUNDS_PLAYER, getAttributes());
            return true;
        }

        Shops.getEconomy().withdrawPlayer(player, amount);
        store.addFunds(amount);
        Notifier.process(sender, DEPOSIT_SUCCESS, getAttributes());
        return true;
    }
}