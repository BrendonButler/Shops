package net.sparkzz.command;

import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
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
        Player player = (Player) sender;
        Store store = InventoryManagementSystem.locateCurrentShop(player);
        int quantity = 0;
        String message = "";

        if (material != null) {
            if (args.length == 3) {
                quantity = (args[2].equalsIgnoreCase("all") ? InventoryManagementSystem.countQuantity((Player) sender, material) : Integer.parseInt(args[2]));

                if (!store.containsMaterial(material)) {
                    sender.sendMessage(String.format("%sThis material doesn't currently exist in the shop, use `/shop add %s` to add this item", RED, material));
                    return true;
                }

                if (quantity < 0 && !player.hasPermission("shops.update.inf-stock")) {
                    sender.sendMessage(String.format("%sYou do not have permission to set infinite stock in your Shop (try using a positive quantity)!", RED));
                    return true;
                }

                if (!InventoryManagementSystem.canRemove(player, material, quantity)) {
                    sender.sendMessage(String.format("%sYou don't have enough of this item to stock the store, try leaving out the quantity and adding it later!", RED));
                    return true;
                }

                store.addItem(material, quantity);
                message = String.format("%sYou have successfully added %s%s%s to the shop!", GREEN, GOLD, (quantity > 0) ? String.valueOf(quantity) + GREEN + " of " + GOLD + material : material, GREEN);
            }

            if (args.length == 6) {
                quantity = (args[5].equalsIgnoreCase("all") ? InventoryManagementSystem.countQuantity((Player) sender, material) : Integer.parseInt(args[5]));

                double buyPrice = Double.parseDouble(args[2]);
                double sellPrice = Double.parseDouble(args[3]);
                int maxQuantity = Integer.parseInt(args[4]);

                if (store.containsMaterial(material)) {
                    sender.sendMessage(String.format("%sThis material already exists in the shop, use `/shop update %s` to update this item", RED, material));
                    return true;
                }

                if (quantity < 0 && !player.hasPermission("shops.update.inf-stock")) {
                    sender.sendMessage(String.format("%sYou do not have permission to set infinite stock in your Shop (try using a positive quantity)!", RED));
                    return true;
                }

                if (!InventoryManagementSystem.canRemove(player, material, quantity)) {
                    sender.sendMessage(String.format("%sYou don't have enough of this item to stock the store, try leaving out the quantity and adding it later!", RED));
                    return true;
                }

                store.addItem(material, quantity, maxQuantity, buyPrice, sellPrice);
                message = String.format("%sYou have successfully added %s%s%s to the shop with a buy price of %s%.2f%s, a sell price of %s%.2f%s, and a max quantity of %s%d%s!", GREEN, GOLD, (quantity > 0) ? String.valueOf(quantity) + GREEN + " of " + GOLD + material : material, GREEN, GOLD, buyPrice, GREEN, GOLD, sellPrice, GREEN, GOLD, maxQuantity, GREEN);
            }

            player.getInventory().removeItem(new ItemStack(material, quantity));
            sender.sendMessage(message);
            return true;
        }

        sender.sendMessage(String.format("%sInvalid material (%s)!", RED, args[1]));
        return false;
    }
}