package net.sparkzz.shops.util;

import net.sparkzz.shops.Store;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Transaction Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    @Order(1)
    void testValidateReady_PurchaseAffordable() {
        prepareEmeraldPurchase(24);
        Transaction transaction = new Transaction(mrSparkzz, new ItemStack(Material.EMERALD, 12), Transaction.TransactionType.PURCHASE);

        assertTrue(transaction.validateReady());
        printSuccessMessage("transaction - purchase ready when affordable");
    }

    @Test
    @DisplayName("Test Transaction - purchase is not ready when the player can't afford it")
    @Order(2)
    void testValidateReady_PurchaseUnaffordable() {
        prepareEmeraldPurchase(23.99);
        Transaction transaction = new Transaction(mrSparkzz, new ItemStack(Material.EMERALD, 12), Transaction.TransactionType.PURCHASE);

        assertFalse(transaction.validateReady());
        printSuccessMessage("transaction - purchase not ready when unaffordable");
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
    @Order(3)
    void testGetType() {
        assertEquals(Transaction.TransactionType.PURCHASE, purchase.getType());
        assertEquals(Transaction.TransactionType.SALE, sale.getType());
        printSuccessMessage("transaction - get type");
    }
}
