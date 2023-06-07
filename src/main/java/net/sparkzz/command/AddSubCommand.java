package net.sparkzz.command;

import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
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
public class AddSubCommand implements ISubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        Material material = Material.matchMaterial(args[1]);
        double buyPrice = Double.parseDouble(args[2]);
        double sellPrice = Double.parseDouble(args[3]);
        int maxQuantity = Integer.parseInt(args[4]);

        int quantity = 0;

        if (args.length == 6)
            quantity = Integer.parseInt(args[5]);

        if (material != null) {
            Player player = (Player) sender;
            Store store = InventoryManagementSystem.locateCurrentShop(player);

            if (store.containsMaterial(material)) {
                sender.sendMessage(String.format("%sThis material already exists in the shop, use `/shop update %s` to update this item", RED, material));
                return true;
            }

            if (!InventoryManagementSystem.canRemove(player, material, quantity)) {
                sender.sendMessage(String.format("%sYou don't have enough of this item to stock the store, try leaving out the quantity and adding it later!", RED));
                return true;
            }

            store.addItem(material, quantity, maxQuantity, buyPrice, sellPrice);
            sender.sendMessage(String.format("%sYou have successfully added %s%s%s to the shop with a buy price of %s%.2f%s, a sell price of %s%.2f%s, and a max quantity of %s%d%s!", GREEN, GOLD, (quantity > 0) ? "" + quantity + GREEN + " of " + GOLD + material : material, GREEN, GOLD, buyPrice, GREEN, GOLD, sellPrice, GREEN, GOLD, maxQuantity, GREEN));
            return true;
        }

        sender.sendMessage(String.format("%sInvalid material (%s)!", RED, args[1]));
        return false;
    }
}