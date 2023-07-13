package net.sparkzz.util;

import net.milkbowl.vault.economy.Economy;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.sparkzz.util.Notifier.CipherKey.*;

public class Transaction {

    private static final Economy econ = Shops.getEconomy();
    private final ItemStack itemStack;
    private final TransactionType type;
    private final Player player;
    private final Store store;
    private final Notifier.MultilineBuilder transactionMessage;
    private boolean transactionReady = false, financesReady = false, inventoryReady = false;
    private double cost;

    public Transaction(Player player, ItemStack itemStack, TransactionType type) {
        this.player = player;
        this.itemStack = itemStack;
        this.type = type;
        this.transactionMessage = new Notifier.MultilineBuilder();

        store = InventoryManagementSystem.locateCurrentShop(player);

        switch (type) {
            case PURCHASE -> cost = (store.getBuyPrice(itemStack.getType()) * itemStack.getAmount());
            case SALE -> cost = (store.getSellPrice(itemStack.getType()) * itemStack.getAmount());
        }
    }

    private void validateFinances() {
        switch (type) {
            case PURCHASE -> {
                if (econ.getBalance(player) >= cost)
                    financesReady = true;

                if (!financesReady) transactionMessage.append(INSUFFICIENT_FUNDS_PLAYER);
            }
            case SALE -> {
                if (store.hasInfiniteFunds() || store.getBalance() >= cost)
                    financesReady = true;

                if (!financesReady) transactionMessage.append(INSUFFICIENT_FUNDS_STORE);
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

                if (!storeIsSelling) transactionMessage.append(NOT_SELLING);
                else if (!canInsertPlayer) transactionMessage.append(INSUFFICIENT_INV_PLAYER);
                else if (!canWithdrawStore) transactionMessage.append(INSUFFICIENT_STOCK_STORE);

                if (storeIsSelling && canInsertPlayer && canWithdrawStore)
                    inventoryReady = true;
            }
            case SALE -> {
                boolean canWithdrawPlayer = player.getInventory().containsAtLeast(itemStack, itemQuantity);
                boolean storeIsBuying = store.containsMaterial(material) && store.getAttributes(material).get("sell").doubleValue() >= 0;
                boolean storeIsBuyingMore = storeIsBuying && InventoryManagementSystem.getAvailableSpace(store, material) >= itemQuantity;

                if (!storeIsBuying) transactionMessage.append(NOT_BUYING);
                else if (!storeIsBuyingMore) transactionMessage.append(NOT_BUYING_ANYMORE);
                else if (!canWithdrawPlayer) transactionMessage.append(INSUFFICIENT_AMOUNT_PLAYER);

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

    public Notifier.MultilineBuilder getMessage() {
        return transactionMessage;
    }

    public void process() {
        switch (type) {
            case PURCHASE -> {
                if (!store.hasInfiniteStock() && store.getAttributes(itemStack.getType()).get("quantity").intValue() >= 0)
                    store.removeItem(itemStack);

                store.addFunds(cost);
                player.getInventory().addItem(itemStack);
                econ.withdrawPlayer(player, cost);
            }
            case SALE -> {
                if (!store.hasInfiniteStock() && store.getAttributes(itemStack.getType()).get("quantity").intValue() >= 0)
                    store.addItem(itemStack);
                if (!store.hasInfiniteFunds())
                    store.removeFunds(cost);

                player.getInventory().removeItem(itemStack);
                econ.depositPlayer(player, cost);
            }
        }
    }

    public enum TransactionType {
        PURCHASE, SALE
    }
}