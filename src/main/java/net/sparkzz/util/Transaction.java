package net.sparkzz.util;

import net.sparkzz.shops.Shops;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Transaction {

    private boolean transactionReady, financesReady = false, inventoryReady = false;
    private double cost;
    private ItemStack itemStack;
    private TransactionType type;
    private Player player;

    public Transaction(Player player, ItemStack itemStack, double cost, TransactionType type) {
        this.player = player;
        this.itemStack = itemStack;
        this.cost = cost;
        this.type = type;
    }

    private void validateFinances() {
        switch (type) {
            case PURCHASE -> {
                if (Shops.econ.getBalance(player) >= cost)
                    financesReady = true;
            }
            case SALE -> {
                // TODO: check shop balance && shop infinite balance flag
                financesReady = true;
            }
        }
    }

    private void validateInventory() {
        switch (type) {
            case PURCHASE -> {
                // TODO: check if shop has or will run out of inventory (or if infinite inventory)
                if (InventoryManagementSystem.canInsert(player, itemStack.getType(), itemStack.getAmount()))
                    inventoryReady = true;
            }
            case SALE -> {
                // TODO: check if shop has or will reach max capacity (or if infinite capacity)
                if (player.getInventory().containsAtLeast(itemStack, itemStack.getAmount()))
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

    public boolean validateReady() {
        boolean valid = false;

        validateFinances();
        validateInventory();

        if (financesReady && inventoryReady) {
            valid = true;
            transactionReady = true;
        }

        return valid;
    }

    public void process() {
        switch (type) {
            case PURCHASE -> {
                player.getInventory().addItem(itemStack);
                Shops.econ.withdrawPlayer(player, cost);
            }
            case SALE -> {
                player.getInventory().remove(itemStack);
                Shops.econ.depositPlayer(player, cost);
            }
        }
    }

    public enum TransactionType {
        PURCHASE, SALE;
    }
}