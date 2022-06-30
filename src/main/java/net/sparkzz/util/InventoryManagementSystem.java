package net.sparkzz.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ListIterator;

/**
 * Helper class to manage player and store inventory
 *
 * @author Brendon Butler
 */
public class InventoryManagementSystem {

    private static int countQuantity(Player player, Material material) {
        ListIterator<ItemStack> iterator = player.getInventory().iterator();
        int quantity = 0;

        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();

            if (stack != null && stack.getType().equals(material))
                quantity += stack.getAmount();
        }

        return quantity;
    }

    private static int getAvailableSpace(Player player, Material material) {
        ListIterator<ItemStack> iterator = player.getInventory().iterator();
        int availableSpace = 0;

        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();

            if (stack == null)
                availableSpace += material.getMaxStackSize();
            else if (stack.getType().equals(material))
                availableSpace += (stack.getMaxStackSize() - stack.getAmount());
        }

        return availableSpace;
    }

    public static boolean canInsert(Player player, Material material, int quantity) {
        int availableSpace = getAvailableSpace(player, material);

        return (quantity <= availableSpace);
    }

    public static boolean canRemove(Player player, Material material, int quantity) {
        int inInventory = countQuantity(player, material);

        return (quantity <= inInventory);
    }
}