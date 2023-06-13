package net.sparkzz.util;

import net.sparkzz.shops.Shops;
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

    public static boolean canInsert(Player player, Material material, int quantity) {
        int availableSpace = getAvailableSpace(player, material);

        return (quantity <= availableSpace);
    }

    public static boolean canRemove(Player player, Material material, int quantity) {
        int inInventory = countQuantity(player, material);

        return (quantity <= inInventory);
    }

    public static boolean containsAtLeast(Store store, ItemStack itemStack) {
        int storeQuantity = countQuantity(store, itemStack.getType());

        return store.hasInfiniteStock() || storeQuantity >= itemStack.getAmount();
    }

    public static int countQuantity(Player player, Material material) {
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

        if (store.containsMaterial(material)) {
            quantity = store.getAttributes(material).get("quantity").intValue();

            quantity = (quantity < 0) ? Integer.MAX_VALUE : quantity;
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

    public static int getAvailableSpace(Store store, Material material) {
        int availableSpace = 0;

        if (!store.hasInfiniteStock() && store.containsMaterial(material)) {
            Map<Material, Map<String, Number>> items = store.getItems();
            int maxQuantity = items.get(material).get("max_quantity").intValue();
            int curQuantity = items.get(material).get("quantity").intValue();

            availableSpace = (curQuantity < 0) ? Integer.MAX_VALUE : maxQuantity - curQuantity;
        } else if (store.hasInfiniteStock() && store.containsMaterial(material))
            availableSpace = Integer.MAX_VALUE;

        return availableSpace;
    }

    public static Store locateCurrentShop(Player player) {
        // TODO: locate the player within the bounds of a current shop
        return Shops.shop;
    }
}