package net.sparkzz.util;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.ChatColor.*;

public class Transaction {

    private boolean transactionReady = false, financesReady = false, inventoryReady = false;
    private double cost;
    private ItemStack itemStack;
    private TransactionType type;
    private Player player;
    private Store store;
    private StringBuilder transactionMessage = new StringBuilder();

    public Transaction(Player player, ItemStack itemStack, TransactionType type) {
        this.player = player;
        this.itemStack = itemStack;
        this.type = type;

        locateCurrentShop();

        switch (type) {
            case PURCHASE:
                cost = (store.getBuyPrice(itemStack.getType()) * itemStack.getAmount());
                break;
            case SALE:
                cost = (store.getSellPrice(itemStack.getType()) * itemStack.getAmount());
        }
    }

    private void locateCurrentShop() {
        // TODO: locate the player within the bounds of a current shop
        this.store = Shops.shop;
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
        switch (type) {
            case PURCHASE -> {
                boolean canInsertPlayer = InventoryManagementSystem.canInsert(player, itemStack.getType(), itemStack.getAmount());
                boolean canWithdrawStore = InventoryManagementSystem.containsAtLeast(store, itemStack) && (InventoryManagementSystem.countQuantity(store, itemStack.getType()) == -1 || InventoryManagementSystem.countQuantity(store, itemStack.getType()) >= itemStack.getAmount());

                if (!canInsertPlayer) transactionMessageBuilder(String.format("%sNot enough inventory space!", RED));
                if (!canWithdrawStore) transactionMessageBuilder(String.format("%sStore has insufficient stock!", RED));

                if (canInsertPlayer && canWithdrawStore)
                    inventoryReady = true;
            }
            case SALE -> {
                // TODO: check if shop has or will reach max capacity (or if infinite capacity)
                boolean canWithdrawPlayer = player.getInventory().containsAtLeast(itemStack, itemStack.getAmount());
                boolean canDepositStore = store.getItems().containsKey(itemStack.getType());

                if (!canWithdrawPlayer) transactionMessageBuilder(String.format("%sYou have an insufficient amount!", RED));
                if (!canDepositStore) transactionMessageBuilder(String.format("%sThe store is not buying any of these at this time!", RED));

                if (canWithdrawPlayer && canDepositStore)
                    inventoryReady = true;
            }
        }
    }

    public boolean isFinancesReady() {
        return financesReady;
    }

    public boolean isInventoryReady() {
        return inventoryReady;
    }

    public boolean isTransactionReady() {
        return transactionReady;
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
                if (!store.hasInfiniteStock())
                    store.removeItem(itemStack);

                store.addFunds(cost);
                player.getInventory().addItem(itemStack);
                Shops.econ.withdrawPlayer(player, cost);
            }
            case SALE -> {
                if (!store.hasInfiniteStock())
                    store.addItem(itemStack);
                if (!store.hasInfiniteFunds())
                    store.removeFunds(cost);

                player.getInventory().removeItem(itemStack);
                Shops.econ.depositPlayer(player, cost);
            }
        }
    }

    public enum TransactionType {
        PURCHASE, SALE;
    }
}