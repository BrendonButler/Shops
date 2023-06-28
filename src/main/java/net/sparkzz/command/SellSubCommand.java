package net.sparkzz.command;

import net.sparkzz.util.InventoryManagementSystem;
import net.sparkzz.util.Transaction;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.ChatColor.*;

/**
 * Sell subcommand used for processing sell transactions
 *
 * @author Brendon Butler
 */
public class SellSubCommand implements ISubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        Material material = Material.matchMaterial(args[1]);

        int quantity = 1;

        if (args.length == 3)
            quantity = (args[2].equalsIgnoreCase("all") ? InventoryManagementSystem.countQuantity((Player) sender, material) : Integer.parseInt(args[2]));

        // quantity less than or equal to 0, or greater than 2304 (max inventory capacity) is invalid
        if (quantity <= 0 || quantity > 2304) {
            sender.sendMessage(String.format("%sInvalid quantity (%d)!", RED, quantity));
            return true;
        }

        if (material != null) {
            Transaction transaction = new Transaction((Player) sender, new ItemStack(material, quantity), Transaction.TransactionType.SALE);

            if (args.length == 2 && transaction.getTotalCost() != -1) {
                sender.sendMessage(String.format("%sPrice: %s%.2f", BLUE, GREEN, transaction.getTotalCost()));
                return true;
            }

            if (!transaction.validateReady()) {
                transaction.getMessage().processIndividual(sender);
                return true;
            }

            transaction.process();
            sender.sendMessage(String.format("%sSuccess! You have sold %s%s%s of %s%s%s for %s$%.2f%s.",
                    GREEN, GOLD, quantity, GREEN, GOLD, material, GREEN, GOLD, transaction.getTotalCost(), GREEN));
            return true;
        }

        return true;
    }
}