package net.sparkzz.shops.util;

import net.sparkzz.shops.Store;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    public static boolean canInsert(Player player, ItemType material, int quantity) {
        int availableSpace = getAvailableSpace(player.inventory(), material);

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
        PlayerInventory inventory = player.inventory();

        return items.stream().allMatch(inventory::canFit);
    }

    /**
     * Checks whether the provided material and quantity can be removed from the player's inventory
     *
     * @param player the player to have their inventory checked
     * @param itemStack the item stack to be checked if it can be removed from the player's inventory
     * @param quantity the quantity of the material to be checked if it can be removed from the store
     * @return whether the provided quantity of material can be removed from the player's inventory
     */
    public static boolean canRemove(Player player, ItemStack itemStack, int quantity) {
        int inInventory = countQuantity(player, itemStack.type());

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
        int storeQuantity = countQuantity(store, itemStack.type());

        return store.hasInfiniteStock() || storeQuantity >= itemStack.quantity();
    }

    /**
     * Counts the quantity of the provided material in the player's inventory
     *
     * @param player the player to have their inventory queried
     * @param material the material to be queried in the player's inventory
     * @return the quantity of the provided material in the player's inventory
     */
    public static int countQuantity(Player player, ItemType material) {
        Iterator<Slot> slotIterator = player.inventory().slots().iterator();
        int quantity = 0;

        while (slotIterator.hasNext()) {
            Slot slot = slotIterator.next();

            if (slot != null && slot.peek().type().equals(material))
                quantity += slot.peek().quantity();
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
    public static int countQuantity(Store store, ItemType material) {
        int quantity = -1;

        if (store.containsItemType(material)) {
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
    private static int getAvailableSpace(PlayerInventory inventory, ItemType material) {
        int availableSpace = 0;

        ArrayList<Slot> slots = new ArrayList<>();
        slots.addAll(inventory.storage().slots());
        slots.addAll(inventory.hotbar().slots());

        for (Slot slot : slots) {
            if (slot.peek().type().equals(material) || slot.peek().isEmpty())
                availableSpace += slot.freeCapacity();
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
    public static int getAvailableSpace(Store store, ItemType material) {
        int availableSpace = 0;

        if (!store.hasInfiniteStock() && store.containsItemType(material)) {
            Map<ItemType, Map<String, Number>> items = store.getItems();
            int maxQuantity = items.get(material).get("max_quantity").intValue();
            int curQuantity = items.get(material).get("quantity").intValue();

            availableSpace = (maxQuantity < 0) ? Integer.MAX_VALUE : maxQuantity - curQuantity;
        } else if (store.hasInfiniteStock() && store.containsItemType(material))
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
        Optional<Store> store = Store.getDefaultStore((ServerWorld) player.world());

        for (Store currentStore : Store.getStores()) {
            if (currentStore.getCuboidLocation() != null && currentStore.getCuboidLocation().isPlayerWithin(player)) {
                store = Optional.of(currentStore);
                break;
            }
        }

        return store;
    }
}