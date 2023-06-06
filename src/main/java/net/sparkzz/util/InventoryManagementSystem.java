package net.sparkzz.util;

import net.sparkzz.shops.Store;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ListIterator;
import java.util.Map;

/**
 * Helper class to manage player and store inventory
 *
 * @author Brendon Butler
 */
public class InventoryManagementSystem {

    public static boolean containsAtLeast(Store store, ItemStack itemStack) {
        int storeQuantity = countQuantity(store, itemStack.getType());

        return store.hasInfiniteStock() || storeQuantity == -1 || (store.getItems().containsKey(itemStack.getType()) && storeQuantity >= itemStack.getAmount());
    }

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

    public static int countQuantity(Store store, Material material) {
        int quantity = -1;

        if (!store.hasInfiniteStock())
            quantity = store.getItems().get(material).get("quantity").intValue();

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

    public static int getAvailableSpace(Store store, Material material) {
        int availableSpace = 0;

        if (!store.hasInfiniteStock()) {
            Map<Material, Map<String, Number>> items = store.getItems();
            int maxQuantity = items.get(material).get("max_quantity").intValue();
            int curQuantity = items.get(material).get("quantity").intValue();

            availableSpace = maxQuantity - curQuantity;
        } else availableSpace = -1;

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