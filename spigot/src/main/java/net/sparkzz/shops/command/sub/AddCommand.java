package net.sparkzz.shops.command.sub;

import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;

/**
 * Add subcommand used for adding items to a shop
 *
 * @author Brendon Butler
 */
public class AddCommand extends SubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        resetAttributes();
        setArgsAsAttributes(args);
        Material material = setAttribute("material", Material.matchMaterial(args[1]));
        Player player = (Player) setAttribute("sender", sender);
        Store store = setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));
        int quantity = setAttribute("quantity", 0);
        String message = "";

        if (store == null) {
            Notifier.process(player, NO_STORE_FOUND, getAttributes());
            return true;
        }

        if (material != null) {
            if (args.length == 3) {
                quantity = setAttribute("quantity", args[2].equalsIgnoreCase("all") ? InventoryManagementSystem.countQuantity((Player) sender, material) : Integer.parseInt(args[2]));

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
                message = Notifier.compose((quantity > 0 ? ADD_SUCCESS_QUANTITY : ADD_SUCCESS), getAttributes());
            }

            if (args.length == 6) {
                quantity = setAttribute("quantity", args[5].equalsIgnoreCase("all") ? InventoryManagementSystem.countQuantity((Player) sender, material) : Integer.parseInt(args[5]));

                double buyPrice = setAttribute("buy-price", Double.parseDouble(args[2]));
                double sellPrice = setAttribute("sell-price", Double.parseDouble(args[3]));
                int maxQuantity = setAttribute("max-quantity", Integer.parseInt(args[4]));

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

            if (quantity <= 0 && message.isBlank()) throw new IllegalArgumentException();
            player.getInventory().removeItem(new ItemStack(material, quantity));
            sender.sendMessage(message);
            return true;
        }

        setAttribute("material", args[1]);
        Notifier.process(sender, INVALID_MATERIAL, getAttributes());
        return false;
    }
}