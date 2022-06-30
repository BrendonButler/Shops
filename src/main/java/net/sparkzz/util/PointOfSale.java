package net.sparkzz.util;

import net.milkbowl.vault.economy.EconomyResponse;
import net.sparkzz.shops.Shops;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Helper class to manage transactions and communications with the IMS
 *
 * @author Brendon Butler
 */
public class PointOfSale {

    // purchase from shop
    public boolean purchase(Player player, Material material, int quantity) {
        // TODO: calculate cost from shop
        EconomyResponse response = Shops.econ.withdrawPlayer(player, quantity);

        return response.transactionSuccess();
    }

    // refund player
    public void refund(Player player, int quantity) {
        Shops.econ.depositPlayer(player, quantity);
    }

    // TODO: Possibly create TransactionResponse class which checks if the transaction is possible, store the result and values to be processed or cancelled (validation)
}