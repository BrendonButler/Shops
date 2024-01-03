package net.sparkzz.shops.util;

import net.sparkzz.shops.Store;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;
import java.util.Optional;

import static org.spongepowered.api.item.inventory.query.QueryTypes.ITEM_TYPE;

/**
 * This helper class provides a transaction handler so that transactions can be built and verified before being
 * processed
 */
public class Transaction extends Notifiable {

    private static final EconomyService econ = Sponge.server().serviceProvider().economyService().orElseThrow();
    private final ItemStack itemStack;
    private final TransactionType type;
    private final Player player;
    private final Store store;
    private final Notifier.MultilineBuilder transactionMessage;
    private boolean transactionReady = false, financesReady = false, inventoryReady = false;
    private final BigDecimal cost;

    /**
     * Constructs the transaction with the player, item stack, and transaction type
     *
     * @param player the player associated with the transaction
     * @param itemStack the item stack associated with the transaction
     * @param type the provided type of transaction
     */
    public Transaction(Player player, ItemStack itemStack, TransactionType type) {
        this.player = (Player) setAttribute("player", player);
        this.itemStack = itemStack;
        this.type = (TransactionType) setAttribute("type", type);
        this.transactionMessage = new Notifier.MultilineBuilder(getAttributes());

        setAttribute("material", itemStack.type());
        setAttribute("quantity", itemStack.quantity());

        store = (Store) setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));
        cost = (BigDecimal) setAttribute("cost", switch (type) {
            case PURCHASE -> (store.getBuyPrice(itemStack.type()).multiply(BigDecimal.valueOf(itemStack.quantity())));
            case SALE -> (store.getSellPrice(itemStack.type()).multiply(BigDecimal.valueOf(itemStack.quantity())));
        });

    }

    private void validateFinances() {
        Optional<UniqueAccount> account = econ.findOrCreateAccount(player.uniqueId());

        switch (type) {
            case PURCHASE -> {
                if (account.orElseThrow().balance(econ.defaultCurrency()).compareTo(cost) >= 0)
                    financesReady = true;

                if (!financesReady) transactionMessage.append(Notifier.CipherKey.INSUFFICIENT_FUNDS_PLAYER);
            }
            case SALE -> {
                if (store.hasInfiniteFunds() || store.getBalance().compareTo(cost) >= 0)
                    financesReady = true;

                if (!financesReady) transactionMessage.append(Notifier.CipherKey.INSUFFICIENT_FUNDS_STORE);
            }
            default -> {}
        }
    }

    private void validateInventory() {
        ItemType material = itemStack.type();
        int itemQuantity = itemStack.quantity();

        switch (type) {
            case PURCHASE -> {
                boolean canInsertPlayer = InventoryManagementSystem.canInsert(player, material, itemQuantity);
                boolean canWithdrawStore = store.containsItemType(material) && InventoryManagementSystem.containsAtLeast(store, itemStack);
                boolean storeIsSelling = store.containsItemType(material) && store.getAttributes(material).get("buy").doubleValue() >= 0;

                if (!storeIsSelling) transactionMessage.append(Notifier.CipherKey.NOT_SELLING);
                else if (!canInsertPlayer) transactionMessage.append(Notifier.CipherKey.INSUFFICIENT_INV_PLAYER);
                else if (!canWithdrawStore) transactionMessage.append(Notifier.CipherKey.INSUFFICIENT_STOCK_STORE);

                if (storeIsSelling && canInsertPlayer && canWithdrawStore)
                    inventoryReady = true;
            }
            case SALE -> {
                boolean canWithdrawPlayer = player.inventory().contains(itemStack);
                boolean storeIsBuying = store.containsItemType(material) && store.getAttributes(material).get("sell").doubleValue() >= 0;
                boolean storeIsBuyingMore = storeIsBuying && InventoryManagementSystem.getAvailableSpace(store, material) >= itemQuantity;

                if (!storeIsBuying) transactionMessage.append(Notifier.CipherKey.NOT_BUYING);
                else if (!storeIsBuyingMore) transactionMessage.append(Notifier.CipherKey.NOT_BUYING_ANYMORE);
                else if (!canWithdrawPlayer) transactionMessage.append(Notifier.CipherKey.INSUFFICIENT_AMOUNT_PLAYER);

                if (storeIsBuying && storeIsBuyingMore && canWithdrawPlayer)
                    inventoryReady = true;
            }
            default -> {}
        }
    }

    /**
     * Validates whether the finances and inventory are ready for both the player and the store
     *
     * @return whether the finances and inventory are ready
     */
    public boolean validateReady() {
        validateFinances();
        validateInventory();

        if (financesReady && inventoryReady)
            transactionReady = true;

        return transactionReady;
    }

    /**
     * Gets the total cost of the transaction
     *
     * @return the total cost of the transaction
     */
    public BigDecimal getTotalCost() {
        return cost;
    }

    /**
     * Gets the transaction message
     *
     * @return the transaction message
     */
    public Notifier.MultilineBuilder getMessage() {
        return transactionMessage;
    }

    /**
     * Gets the transaction type
     *
     * @return the transaction type
     */
    public TransactionType type() {
        return type;
    }

    /**
     * Processes the transaction for the player and store
     */
    public void process() {
        Optional<UniqueAccount> account = econ.findOrCreateAccount(player.uniqueId());

        switch (type) {
            case PURCHASE -> {
                if (!store.hasInfiniteStock() && store.getAttributes(itemStack.type()).get("quantity").intValue() >= 0)
                    store.removeItem(itemStack);

                store.addFunds(cost);
                player.inventory().offer(itemStack);
                account.orElseThrow().withdraw(econ.defaultCurrency(), cost);
            }
            case SALE -> {
                if (!store.hasInfiniteStock() && store.getAttributes(itemStack.type()).get("quantity").intValue() >= 0)
                    store.addItem(itemStack);
                if (!store.hasInfiniteFunds())
                    store.removeFunds(cost);

                player.inventory().query(ITEM_TYPE.get().of(itemStack.type())).poll(itemStack.quantity());
                account.orElseThrow().deposit(econ.defaultCurrency(), cost);
            }
            default -> {}
        }
    }

    /**
     * The transaction type determines how the transaction should be processed
     */
    public enum TransactionType {
        PURCHASE, SALE
    }
}