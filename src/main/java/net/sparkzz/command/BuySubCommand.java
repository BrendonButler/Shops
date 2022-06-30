package net.sparkzz.command;

import net.sparkzz.util.Transaction;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.ChatColor.*;

/**
 * Buy subcommand used for processing buy transactions
 *
 * @author Brendon Butler
 */
public class BuySubCommand implements ISubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        Material material = Material.matchMaterial(args[1]);

        int quantity = 1;

        if (args.length == 3)
            quantity = Integer.parseInt(args[2]);

        if (material != null) {
            Transaction transaction = new Transaction((Player) sender, new ItemStack(material, quantity), quantity, Transaction.TransactionType.PURCHASE);
            transaction.validateReady();
            boolean financialResult = transaction.isFinancesReady();
            boolean inventoryResult = transaction.isInventoryReady();

            if (!financialResult && !inventoryResult) {
                sender.sendMessage(String.format("%sInsufficient funds and inventory space!", RED));
                return true;
            }

            if (financialResult && !inventoryResult) {
                sender.sendMessage(String.format("%sInsufficient inventory space!", RED));
                return true;
            }

            if (!financialResult && inventoryResult) {
                sender.sendMessage(String.format("%sInsufficient funds!", RED));
                return true;
            }

            transaction.process();
            sender.sendMessage(String.format("%sSuccess! You have purchased %s%s%s of %s%s%s for %s$%s%s.",
                    GREEN, GOLD, quantity, GREEN, GOLD, material, GREEN, GOLD, quantity, GREEN));
            return true;
        }

        return false;
    }
}