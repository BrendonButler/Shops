package net.sparkzz.command;

import net.sparkzz.util.Transaction;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.ChatColor.*;
import static org.bukkit.ChatColor.GREEN;

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
            quantity = Integer.parseInt(args[2]);

        if (material != null) {
            Transaction transaction = new Transaction((Player) sender, new ItemStack(material, quantity), quantity, Transaction.TransactionType.SALE);
            transaction.validateReady();
            boolean financialResult = transaction.isFinancesReady();
            boolean inventoryResult = transaction.isInventoryReady();

            if (!financialResult && !inventoryResult) {
                sender.sendMessage(String.format("%sInsufficient funds and inventory!", RED));
                return true;
            }

            if (financialResult && !inventoryResult) {
                sender.sendMessage(String.format("%sInsufficient inventory!", RED));
                return true;
            }

            if (!financialResult && inventoryResult) {
                sender.sendMessage(String.format("%sInsufficient funds!", RED));
                return true;
            }

            transaction.process();
            sender.sendMessage(String.format("%sSuccess! You have sold %s%s%s of %s%s%s for %s$%s%s.",
                    GREEN, GOLD, quantity, GREEN, GOLD, material, GREEN, GOLD, quantity, GREEN));
            return true;
        }

        return true;
    }
}