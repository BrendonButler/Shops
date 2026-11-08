package net.sparkzz.shops.util;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.FakeEconomy;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.math.BigDecimal;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Transaction Test")
public class TransactionTest {

    private static ItemStack emeralds;
    private static PlayerMock mrSparkzz, player;
    private static ServerMock server;
    private static Store store;
    private static Transaction purchase, sale;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST TRANSACTION ]==");
        server = startServer();
        emeralds = new ItemStack(Material.EMERALD, 64);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player = server.addPlayer();

        mrSparkzz.setOp(true);
        Store.setDefaultStore(mrSparkzz.getWorld(), (store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
    }

    @AfterAll
    static void tearDownAll() {
        stopServer();
    }

    @BeforeEach
    void setUpTransactions() {
        store.addItem(emeralds);
        mrSparkzz.getInventory().addItem(emeralds);

        purchase = new Transaction(mrSparkzz, new ItemStack(Material.EMERALD, 12), Transaction.TransactionType.PURCHASE);
        sale = new Transaction(mrSparkzz, emeralds, Transaction.TransactionType.SALE);
    }

    @AfterEach
    void tearDownTransactions() {
        purchase = sale = null;

        store.getItems().clear();
        mrSparkzz.getInventory().clear();
    }

    @Test
    @DisplayName("Test Transaction - purchase is ready when the player can afford it")
    void testValidateReady_PurchaseAffordable() {
        prepareEmeraldPurchase(24);
        Transaction transaction = new Transaction(mrSparkzz, new ItemStack(Material.EMERALD, 12), Transaction.TransactionType.PURCHASE);

        assertTrue(transaction.validateReady());
        printSuccessMessage("transaction - purchase ready when affordable");
    }

    @Test
    @DisplayName("Test Transaction - purchase is not ready when the player can't afford it")
    void testValidateReady_PurchaseUnaffordable() {
        prepareEmeraldPurchase(23.99);
        Transaction transaction = new Transaction(mrSparkzz, new ItemStack(Material.EMERALD, 12), Transaction.TransactionType.PURCHASE);

        assertFalse(transaction.validateReady());
        printSuccessMessage("transaction - purchase not ready when unaffordable");
    }

    @Test
    @DisplayName("Test Transaction - purchase changes nothing when the economy rejects the withdrawal")
    void testProcess_PurchaseWithdrawRejected() {
        prepareEmeraldPurchase(24);
        BigDecimal storeBalance = store.getBalance();
        // The transaction picks up the economy when it's created, so create it while the rejecting one is set
        withRejectingEconomy(() -> assertFalse(new Transaction(mrSparkzz, new ItemStack(Material.EMERALD, 12), Transaction.TransactionType.PURCHASE).process()));
        assertEquals(64, store.getItems().get(Material.EMERALD).get("quantity").intValue());
        assertEquals(storeBalance, store.getBalance());
        assertFalse(mrSparkzz.getInventory().contains(Material.EMERALD));
        printSuccessMessage("transaction - purchase rejected by economy");
    }

    @Test
    @DisplayName("Test Transaction - sale changes nothing when the economy rejects the deposit")
    void testProcess_SaleDepositRejected() {
        store.getItems().clear();
        store.addItem(Material.EMERALD, 0, -1, 2D, 1.5D);
        store.setBalance(BigDecimal.valueOf(100));
        mrSparkzz.getInventory().clear();
        mrSparkzz.getInventory().addItem(new ItemStack(Material.EMERALD, 12));
        // The transaction picks up the economy when it's created, so create it while the rejecting one is set
        withRejectingEconomy(() -> assertFalse(new Transaction(mrSparkzz, new ItemStack(Material.EMERALD, 12), Transaction.TransactionType.SALE).process()));
        assertEquals(0, store.getItems().get(Material.EMERALD).get("quantity").intValue());
        assertEquals(BigDecimal.valueOf(100), store.getBalance());
        assertTrue(mrSparkzz.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), 12));
        printSuccessMessage("transaction - sale rejected by economy");
    }

    /**
     * Runs an action with an economy that rejects every withdrawal and deposit, then restores the original
     */
    private static void withRejectingEconomy(Runnable action) {
        Economy original = Shops.getEconomy();

        Shops.setEconomy(new FakeEconomy() {
            @Override
            public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
                return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Rejected");
            }

            @Override
            public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
                return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Rejected");
            }
        });

        try {
            action.run();
        } finally {
            Shops.setEconomy(original);
        }
    }

    /**
     * Stocks the store with 64 emeralds for sale at 2 each (12 cost 24), clears the player's inventory so they
     * have space, and sets the player's balance
     */
    private static void prepareEmeraldPurchase(double balance) {
        store.getItems().clear();
        store.addItem(Material.EMERALD, 64, -1, 2D, 1.5D);
        mrSparkzz.getInventory().clear();
        setBalance(mrSparkzz, balance);
    }

    @Test
    @DisplayName("Test Transaction - get type")
    void testGetType() {
        assertEquals(Transaction.TransactionType.PURCHASE, purchase.getType());
        assertEquals(Transaction.TransactionType.SALE, sale.getType());
        printSuccessMessage("transaction - get type");
    }
}
