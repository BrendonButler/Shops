package net.sparkzz.shops.command.sub;

import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import net.sparkzz.shops.util.Transaction;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;

/**
 * Buy subcommand used for processing buy transactions
 *
 * @author Brendon Butler
 */
public class BuyCommand extends SubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        resetAttributes();
        setArgsAsAttributes(args);
        Material material = (Material) setAttribute("material", Material.matchMaterial(args[1]));
        Player player = (Player) setAttribute("sender", sender);
        int quantity = (Integer) setAttribute("quantity", 1);
        Store store = (Store) setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));

        if (store == null) {
            Notifier.process(player, NO_STORE_FOUND, getAttributes());
            return true;
        }

        if (args.length == 3)
            quantity = (Integer) setAttribute("quantity", Integer.parseInt(args[2]));

        // quantity less than or equal to 0, or greater than 2304 (max inventory capacity) is invalid
        if (quantity <= 0 || quantity > 2304) {
            Notifier.process(sender, INVALID_QUANTITY, getAttributes());
            return true;
        }

        if (material != null) {
            Transaction transaction = new Transaction((Player) sender, new ItemStack(material, quantity), Transaction.TransactionType.PURCHASE);
            setAttribute("cost", transaction.getTotalCost());

            if (args.length == 2 && transaction.getTotalCost().compareTo(BigDecimal.valueOf(-1)) != 0) {
                Notifier.process(sender, PRICE, getAttributes());
                return true;
            }

            if (!transaction.validateReady()) {
                transaction.getMessage().processIndividual(sender);
                return true;
            }

            transaction.process();
            Notifier.process(sender, BUY_SUCCESS, getAttributes());
            return true;
        }

        setAttribute("material", args[1]);
        Notifier.process(sender, INVALID_MATERIAL, getAttributes());
        return false;
    }
}