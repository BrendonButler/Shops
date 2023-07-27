package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
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
 * Remove subcommand used for removing items from a shop
 *
 * @author Brendon Butler
 */
public class RemoveCommand extends SubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        resetAttributes();
        setArgsAsAttributes(args);
        Material material = (Material) setAttribute("material", Material.matchMaterial(args[1]));
        Player player = (Player) setAttribute("sender", sender);
        Store store = InventoryManagementSystem.locateCurrentStore(player);
        setAttribute("store", store.getName());

        int quantity = 0;

        if (args.length == 3)
            quantity = (args[2].equalsIgnoreCase("all") ? InventoryManagementSystem.countQuantity(store, material) : Integer.parseInt(args[2]));

        if (material != null) {
            if (!store.containsMaterial(material)) {
                Notifier.process(sender, MATERIAL_MISSING_STORE, getAttributes());
                return true;
            }

            int moveQuantity = (Integer) setAttribute("quantity", (quantity <= 0) ? store.getAttributes(material).get("quantity").intValue() : quantity);

            if (!InventoryManagementSystem.containsAtLeast(store, new ItemStack(material, moveQuantity))) {
                Notifier.process(sender, INSUFFICIENT_INV_STORE, getAttributes());
                return true;
            }

            if (!InventoryManagementSystem.canInsert(player, material, moveQuantity)) {
                Notifier.process(sender, REMOVE_INSUFFICIENT_INV_PLAYER, getAttributes());
                return true;
            }

            if (quantity > 0)
                store.removeItem(material, quantity);
            else store.removeItem(material);

            player.getInventory().addItem(new ItemStack(material, moveQuantity));

            Notifier.process(sender, (quantity > 0 ? REMOVE_SUCCESS_QUANTITY : REMOVE_SUCCESS), getAttributes());
            return true;
        }

        setAttribute("material", args[1]);
        Notifier.process(sender, INVALID_MATERIAL, getAttributes());
        return false;
    }
}