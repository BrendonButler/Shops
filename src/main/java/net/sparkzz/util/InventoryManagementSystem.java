package net.sparkzz.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Helper class to manage player and store inventory
 *
 * @author Brendon Butler
 */
public class InventoryManagementSystem {

    public boolean insert(Player player, Material material, int quantity) {
        // TODO: check inventory before adding items return if space is sufficient
        Transaction transaction = new Transaction(player, new ItemStack(material, quantity), quantity, Transaction.TransactionType.SALE);


        player.getInventory().addItem(new ItemStack(material, quantity));
        return true;
    }

    public void takeBack(Player player, Material material, int quantity) {
        player.getInventory().removeItem(new ItemStack(material, quantity));
    }

    // TODO: Possibly create TransactionResponse class which checks if the transaction is possible, store the result and values to be processed or cancelled (validation)
}