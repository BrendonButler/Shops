package net.sparkzz.util;

import net.sparkzz.shops.Shops;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Transaction {

    private boolean transactionReady;
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

    public boolean validateFinances() {
        boolean valid = false;

        switch (type) {
            case PURCHASE -> {
                if (Shops.econ.getBalance(player) >= cost)
                    valid = true;
            }
            case SALE -> {
                // TODO: check shop balance && shop infinite balance flag
                valid = true;
            }
        }

        return valid;
    }

    public boolean validateInventory() {
        boolean valid = false;

        switch (type) {
            case PURCHASE -> {
                // TODO: check if shop has or will run out of inventory (or if infinite inventory)
                // TODO: check player inventory for free space (use InventoryManagementSystem)
                valid = true;
            }
            case SALE -> {
                // TODO: check if shop has or will reach max capacity (or if infinite capacity)
                if (player.getInventory().containsAtLeast(itemStack, itemStack.getAmount()))
                    valid = true;
            }
        }

        return valid;
    }

    public boolean validateReady() {
        boolean valid = false;

        if (validateFinances() && validateInventory()) {
            valid = true;
            transactionReady = true;
        }

        return valid;
    }

    public void process() {
        switch (type) {
            case PURCHASE -> {
                player.getInventory().remove(itemStack);
                Shops.econ.withdrawPlayer(player, cost);
            }
            case SALE -> {
                player.getInventory().addItem(itemStack);
                Shops.econ.depositPlayer(player, cost);
            }
        }
    }

    protected enum TransactionType {
        PURCHASE, SALE;
    }
}