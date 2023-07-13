package net.sparkzz.command;

import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
import net.sparkzz.util.Notifier;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.sparkzz.util.Notifier.CipherKey.*;

/**
 * Add subcommand used for adding items to a shop
 *
 * @author Brendon Butler
 */
public class AddSubCommand extends ISubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        resetAttributes();
        setArgsAsAttributes(args);
        Material material = (Material) setAttribute("material", Material.matchMaterial(args[1]));
        Player player = (Player) setAttribute("sender", sender);
        Store store = (Store) setAttribute("store", InventoryManagementSystem.locateCurrentShop(player));
        int quantity = (Integer) setAttribute("quantity", 0);
        String message = "";

        if (material != null) {
            if (args.length == 3) {
                quantity = (int) setAttribute("quantity", args[2].equalsIgnoreCase("all") ? InventoryManagementSystem.countQuantity((Player) sender, material) : Integer.parseInt(args[2]));

                if (!store.containsMaterial(material)) {
                    Notifier.process(sender, MATERIAL_MISSING_STORE, getAttributes());
                    return true;
                }

                if (quantity < 0 && !player.hasPermission("shops.update.inf-stock")) {
                    Notifier.process(sender, NO_PERMS_INF_STOCK, getAttributes());
                    return true;
                }

                if (!InventoryManagementSystem.canRemove(player, material, quantity)) {
                    Notifier.process(sender, INSUFFICIENT_STOCK_PLAYER, getAttributes());
                    return true;
                }

                store.addItem(material, quantity);
                message = Notifier.compose((quantity > 0 ? ADDED_TO_STORE_QUANTITY : ADDED_TO_STORE), getAttributes());
            }

            if (args.length == 6) {
                quantity = (int) setAttribute("quantity", args[5].equalsIgnoreCase("all") ? InventoryManagementSystem.countQuantity((Player) sender, material) : Integer.parseInt(args[5]));

                double buyPrice = (double) setAttribute("buy-price", Double.parseDouble(args[2]));
                double sellPrice = (double) setAttribute("sell-price", Double.parseDouble(args[3]));
                int maxQuantity = (int) setAttribute("max-quantity", Integer.parseInt(args[4]));

                if (store.containsMaterial(material)) {
                    Notifier.process(sender, MATERIAL_EXISTS_STORE, getAttributes());
                    return true;
                }

                if (quantity < 0 && !player.hasPermission("shops.update.inf-stock")) {
                    Notifier.process(sender, NO_PERMS_INF_STOCK, getAttributes());
                    return true;
                }

                if (!InventoryManagementSystem.canRemove(player, material, quantity)) {
                    Notifier.process(sender, INSUFFICIENT_STOCK_PLAYER, getAttributes());
                    return true;
                }

                store.addItem(material, quantity, maxQuantity, buyPrice, sellPrice);
                message = Notifier.compose((quantity > 0 ? ADDED_MATERIAL_TO_STORE_QUANTITY : ADDED_MATERIAL_TO_STORE), getAttributes());
            }

            player.getInventory().removeItem(new ItemStack(material, quantity));
            sender.sendMessage(message);
            return true;
        }

        Notifier.process(sender, INVALID_MATERIAL, getAttributes());
        return false;
    }
}