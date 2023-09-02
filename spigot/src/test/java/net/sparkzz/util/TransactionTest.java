package net.sparkzz.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.core.shops.Store;
import net.sparkzz.core.util.Transaction;
import net.sparkzz.mocks.MockVault;
import net.sparkzz.shops.Shops;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.*;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Transaction Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionTest {

    private static final ItemStack emeralds = new ItemStack(Material.EMERALD, 64);
    private static PlayerMock mrSparkzz, player;
    private static ServerMock server;
    private static Store store;
    private static Transaction purchase, sale;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST TRANSACTION ]==");
        server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player = server.addPlayer();
        loadConfig();

        mrSparkzz.setOp(true);
        Store.setDefaultStore(mrSparkzz.getWorld(), (store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
    }

    @AfterAll
    static void tearDownAll() {
        MockBukkit.unmock();
        unLoadConfig();
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
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
    @Disabled("Disabled until essentials can be loaded")
    @DisplayName("Test Transaction - shop tab complete")
    @Order(1)
    void testValidateComplete() {
        boolean isReady = purchase.validateReady();
        assertTrue(isReady);
        printSuccessMessage("transaction - validate ready");
    }

    @Test
    @DisplayName("Test Transaction - get type")
    @Order(2)
    void testGetType() {
        assertEquals(Transaction.TransactionType.PURCHASE, purchase.getType());
        assertEquals(Transaction.TransactionType.SALE, sale.getType());
        printSuccessMessage("transaction - get type");
    }
}
