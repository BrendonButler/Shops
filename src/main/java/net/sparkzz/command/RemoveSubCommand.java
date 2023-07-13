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
 * Remove subcommand used for removing items from a shop
 *
 * @author Brendon Butler
 */
public class RemoveSubCommand extends ISubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        Material material = Material.matchMaterial(args[1]);
        Player player = (Player) sender;
        Store store = InventoryManagementSystem.locateCurrentShop(player);

        int quantity = 0;

        if (args.length == 3)
            quantity = (args[2].equalsIgnoreCase("all") ? InventoryManagementSystem.countQuantity(store, material) : Integer.parseInt(args[2]));

        if (material != null) {
            if (!store.containsMaterial(material)) {
                sender.sendMessage(String.format("%sThis material (%s) does not currently exist in the shop!", RED, material));
                return true;
            }

            int moveQuantity = (quantity <= 0) ? store.getAttributes(material).get("quantity").intValue() : quantity;

            if (!InventoryManagementSystem.containsAtLeast(store, new ItemStack(material, moveQuantity))) {
                sender.sendMessage(String.format("%sThe Shop currently doesn't have enough %s%s%s!", RED, GOLD, material, RED));
                return true;
            }

            if (!InventoryManagementSystem.canInsert(player, material, moveQuantity)) {
                sender.sendMessage(String.format("%sYou don't have enough inventory space to remove %s%s%s from your shop, please try specifying a quantity then removing once the shop quantity is lesser!", RED, GOLD, moveQuantity + " " + material, RED));
                return true;
            }

            if (quantity > 0)
                store.removeItem(material, quantity);
            else store.removeItem(material);

            player.getInventory().addItem(new ItemStack(material, moveQuantity));

            sender.sendMessage(String.format("%sYou have successfully removed %s%s%s from the shop!", GREEN, GOLD, (quantity > 0) ? String.valueOf(quantity) + GREEN + " of " + GOLD + material : material, GREEN));
            return true;
        }

        sender.sendMessage(String.format("%sInvalid material (%s)!", RED, args[1]));
        return false;
    }
}