package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
import net.sparkzz.util.InventoryManagementSystem;
import net.sparkzz.util.Notifier;
import net.sparkzz.util.Transaction;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.sparkzz.util.Notifier.CipherKey.*;

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
        setAttribute("store", InventoryManagementSystem.locateCurrentShop(player));

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

            if (args.length == 2 && transaction.getTotalCost() != -1) {
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