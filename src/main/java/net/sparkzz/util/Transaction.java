package net.sparkzz.util;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.ChatColor.RED;

public class Transaction {

    private final ItemStack itemStack;
    private final TransactionType type;
    private final Player player;
    private final Store store;
    private final StringBuilder transactionMessage = new StringBuilder();
    private boolean transactionReady = false, financesReady = false, inventoryReady = false;
    private double cost;

    public Transaction(Player player, ItemStack itemStack, TransactionType type) {
        this.player = player;
        this.itemStack = itemStack;
        this.type = type;

        store = InventoryManagementSystem.locateCurrentShop(player);

        switch (type) {
            case PURCHASE -> cost = (store.getBuyPrice(itemStack.getType()) * itemStack.getAmount());
            case SALE -> cost = (store.getSellPrice(itemStack.getType()) * itemStack.getAmount());
        }
    }

    private void transactionMessageBuilder(String message) {
        if (!transactionMessage.isEmpty())
            transactionMessage.append(System.getProperty("line.separator"));

        transactionMessage.append(message);
    }

    private void validateFinances() {
        switch (type) {
            case PURCHASE -> {
                if (Shops.econ.getBalance(player) >= cost)
                    financesReady = true;

                if (!financesReady) transactionMessageBuilder(String.format("%sYou have insufficient funds!", RED));
            }
            case SALE -> {
                if (store.hasInfiniteFunds() || store.getBalance() >= cost)
                    financesReady = true;

                if (!financesReady) transactionMessageBuilder(String.format("%sStore has insufficient funds!", RED));
            }
        }
    }

    private void validateInventory() {
        Material material = itemStack.getType();
        int itemQuantity = itemStack.getAmount();

        switch (type) {
            case PURCHASE -> {
                boolean canInsertPlayer = InventoryManagementSystem.canInsert(player, material, itemQuantity);
                boolean canWithdrawStore = store.containsMaterial(material) && InventoryManagementSystem.containsAtLeast(store, itemStack);
                boolean storeIsSelling = store.containsMaterial(material) && store.getAttributes(material).get("buy").doubleValue() >= 0;

                if (!storeIsSelling) transactionMessageBuilder(String.format("%sThe Store is not currently selling any of these at this time!", RED));
                else if (!canInsertPlayer) transactionMessageBuilder(String.format("%sYou have insufficient inventory space!", RED));
                else if (!canWithdrawStore) transactionMessageBuilder(String.format("%sThe Store has insufficient stock!", RED));

                if (storeIsSelling && canInsertPlayer && canWithdrawStore)
                    inventoryReady = true;
            }
            case SALE -> {
                boolean canWithdrawPlayer = player.getInventory().containsAtLeast(itemStack, itemQuantity);
                boolean storeIsBuying = store.containsMaterial(material) && store.getAttributes(material).get("sell").doubleValue() >= 0;
                boolean storeIsBuyingMore = storeIsBuying && InventoryManagementSystem.getAvailableSpace(store, material) >= itemQuantity;

                if (!storeIsBuying) transactionMessageBuilder(String.format("%sThe store is not buying any of these at this time!", RED));
                else if (!storeIsBuyingMore) transactionMessageBuilder(String.format("%sThe store is not buying any more of these at this time!", RED));
                else if (!canWithdrawPlayer) transactionMessageBuilder(String.format("%sYou have an insufficient amount!", RED));

                if (storeIsBuying && storeIsBuyingMore && canWithdrawPlayer)
                    inventoryReady = true;
            }
        }
    }

    public boolean validateReady() {
        validateFinances();
        validateInventory();

        if (financesReady && inventoryReady)
            transactionReady = true;

        return transactionReady;
    }

    public double getTotalCost() {
        return cost;
    }

    public String getTransactionMessage() {
        return transactionMessage.toString();
    }

    public void process() {
        switch (type) {
            case PURCHASE -> {
                if (!store.hasInfiniteStock() && store.getAttributes(itemStack.getType()).get("quantity").intValue() >= 0)
                    store.removeItem(itemStack);

                store.addFunds(cost);
                player.getInventory().addItem(itemStack);
                Shops.econ.withdrawPlayer(player, cost);
            }
            case SALE -> {
                if (!store.hasInfiniteStock() && store.getAttributes(itemStack.getType()).get("quantity").intValue() >= 0)
                    store.addItem(itemStack);
                if (!store.hasInfiniteFunds())
                    store.removeFunds(cost);

                player.getInventory().removeItem(itemStack);
                Shops.econ.depositPlayer(player, cost);
            }
        }
    }

    public enum TransactionType {
        PURCHASE, SALE
    }
}