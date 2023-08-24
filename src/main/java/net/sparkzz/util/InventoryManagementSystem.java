package net.sparkzz.util;

import net.sparkzz.shops.Store;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

/**
 * Helper class to manage player and store inventory
 *
 * @author Brendon Butler
 */
public class InventoryManagementSystem {

    /**
     * Checks whether the provided material and quantity can be added to the player's inventory
     *
     * @param player the player to have their inventory checked
     * @param material the material to be checked if it can be added to the player's inventory
     * @param quantity the quantity of the material to be checked if it can be added to the store
     * @return whether the provided quantity of material can be added to the player's inventory
     */
    public static boolean canInsert(Player player, Material material, int quantity) {
        int availableSpace = getAvailableSpace(player.getInventory(), material);

        return (quantity <= availableSpace);
    }

    /**
     * Checks whether the provided material and quantity can be added to the player's inventory
     *
     * @param player the player to have their inventory checked
     * @param items the item stacks to be checked if they can be added to the player's inventory
     * @return whether the provided quantity of material can be added to the player's inventory
     */
    public static boolean canInsertAll(Player player, List<ItemStack> items) {
        PlayerInventory inventory = player.getInventory();
        boolean canInsertAll = true;

        for (ItemStack item : items) {
            int availableSpace = getAvailableSpace(inventory, item.getType());

            if (item.getAmount() <= availableSpace)
                inventory.addItem(item);
            else canInsertAll = false;
        }

        return canInsertAll;
    }

    /**
     * Checks whether the provided material and quantity can be removed from the player's inventory
     *
     * @param player the player to have their inventory checked
     * @param material the material to be checked if it can be removed from the player's inventory
     * @param quantity the quantity of the material to be checked if it can be removed from the store
     * @return whether the provided quantity of material can be removed from the player's inventory
     */
    public static boolean canRemove(Player player, Material material, int quantity) {
        int inInventory = countQuantity(player, material);

        return (quantity <= inInventory);
    }

    /**
     * Checks if the store contains at least the quantity of materials in the item stack
     *
     * @param store the store to have its inventory queried
     * @param itemStack the item stack to be used in the query
     * @return whether the store contains at least the quantity of materials in the item stack
     */
    public static boolean containsAtLeast(Store store, ItemStack itemStack) {
        int storeQuantity = countQuantity(store, itemStack.getType());

        return store.hasInfiniteStock() || storeQuantity >= itemStack.getAmount();
    }

    /**
     * Counts the quantity of the provided material in the player's inventory
     *
     * @param player the player to have their inventory queried
     * @param material the material to be queried in the player's inventory
     * @return the quantity of the provided material in the player's inventory
     */
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

    /**
     * Counts the quantity of the provided material in the store
     *
     * @param store the store to have its inventory queried
     * @param material the material to be queried in the store
     * @return the quantity of the provided material in the player's inventory
     */
    public static int countQuantity(Store store, Material material) {
        int quantity = -1;

        if (store.containsMaterial(material)) {
            quantity = store.getAttributes(material).get("quantity").intValue();

            quantity = (quantity < 0) ? Integer.MAX_VALUE : quantity;
        }

        return quantity;
    }

    /**
     * Gets the available space in the player's inventory based on the material's max stack size, it will even check
     * partial stacks of the input material
     *
     * @param inventory the player's inventory to be queried
     * @param material the material to be used to query the player's inventory
     * @return the available space based on the material's stack size and inventory space
     */
    private static int getAvailableSpace(PlayerInventory inventory, Material material) {
        int availableSpace = 0;

        for (int i = 0; i <= 35; i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack == null)
                availableSpace += material.getMaxStackSize();
            else if (stack.getType().equals(material))
                availableSpace += (stack.getMaxStackSize() - stack.getAmount());
        }

        return availableSpace;
    }

    /**
     * Gets the available space in the store
     *
     * @param store the store to have its inventory queried
     * @param material the material to be used to query the store
     * @return the available space based on the max quantity and current quantity of the provided material
     */
    public static int getAvailableSpace(Store store, Material material) {
        int availableSpace = 0;

        if (!store.hasInfiniteStock() && store.containsMaterial(material)) {
            Map<Material, Map<String, Number>> items = store.getItems();
            int maxQuantity = items.get(material).get("max_quantity").intValue();
            int curQuantity = items.get(material).get("quantity").intValue();

            availableSpace = (maxQuantity < 0) ? Integer.MAX_VALUE : maxQuantity - curQuantity;
        } else if (store.hasInfiniteStock() && store.containsMaterial(material))
            availableSpace = Integer.MAX_VALUE;

        return availableSpace;
    }

    /**
     * Gets the current store based on the player's location
     *
     * @param player the player to have its location checked for the current store
     * @return the store the player is currently located in
     */
    public static Optional<Store> locateCurrentStore(Player player) {
        Optional<Store> store = Store.getDefaultStore(player.getWorld());

        for (Store currentStore : Store.STORES) {
            if (currentStore.getCuboidLocation() != null && currentStore.getCuboidLocation().isPlayerWithin(player)) {
                store = Optional.of(currentStore);
                break;
            }
        }

        return store;
    }
}