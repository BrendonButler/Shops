package net.sparkzz.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.core.shops.Store;
import net.sparkzz.core.util.Notifier;
import net.sparkzz.mocks.MockVault;
import net.sparkzz.shops.Shops;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.Map;

import static net.sparkzz.core.util.Notifier.CipherKey.*;
import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Sell Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SellCommandTest {
    private static final ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

    private static PlayerMock mrSparkzz, player2;
    private static Store store;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST SELL COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);
        loadConfig();

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Store.setDefaultStore(mrSparkzz.getWorld(), (store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        unLoadConfig();
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
    }

    @BeforeEach
    void setUpSellCommand() {
        Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().clear();
        Store.getDefaultStore(mrSparkzz.getWorld()).get().addItem(emeralds.getType(), 0, -1, 2D, 1.5D);
        Store.getDefaultStore(mrSparkzz.getWorld()).get().setBalance(100);
        mrSparkzz.getInventory().addItem(emeralds);
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
    }

    @AfterEach
    void tearDownEach() {
        mrSparkzz.getInventory().clear();
    }

    @Test
    @DisplayName("Test Sell - permissions")
    @Order(1)
    void testSellCommand_Permissions() {
        performCommand(player2, "shop sell emerald 1");
        assertEquals(Notifier.compose(NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("sell command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Sell - main functionality - sell 1")
    @Order(2)
    void testSellCommand() {
        performCommand(mrSparkzz, "shop sell emerald 1");
        assertEquals(Notifier.compose(SELL_SUCCESS, Map.of("quantity", 1, "material", Material.EMERALD, "cost", 1.5D)), mrSparkzz.nextMessage());
        assertEquals(25, Store.getDefaultStore(mrSparkzz.getWorld()).get().getBalance());
        assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test");
    }

    @Test
    @DisplayName("Test Sell - main functionality - below minimum amount")
    @Order(3)
    void testSellCommand_BelowMinimum() {
        performCommand(mrSparkzz, "shop sell emerald -1");
        assertEquals(Notifier.compose(INVALID_QUANTITY, Collections.singletonMap("quantity", -1)), mrSparkzz.nextMessage());
        assertEquals(100, store.getBalance());
        // TODO: assertEquals(0, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - below minimum amount");
    }

    @Test
    @DisplayName("Test Sell - main functionality - above maximum amount")
    @Order(4)
    void testSellCommand_AboveMaximum() {
        performCommand(mrSparkzz, "shop sell emerald 2305");
        assertEquals(Notifier.compose(INVALID_QUANTITY, Collections.singletonMap("quantity", 2305)), mrSparkzz.nextMessage());
        assertEquals(100, store.getBalance());
        // TODO: assertEquals(0, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - above maximum amount");
    }

    @Test
    @DisplayName("Test Sell - main functionality - invalid material")
    @Order(5)
    void testSellCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop sell emeral 1");
        assertEquals(Notifier.compose(INVALID_MATERIAL, Collections.singletonMap("material", "emeral")), mrSparkzz.nextMessage());
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        assertEquals(100, store.getBalance());
        // TODO: assertEquals(0, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - above maximum amount");
    }

    @Test
    @DisplayName("Test Sell - main functionality - query price")
    @Order(6)
    void testSellCommand_QueryPrice() {
        performCommand(mrSparkzz, "shop sell emerald");
        assertEquals(Notifier.compose(PRICE, Collections.singletonMap("cost", 1.5D)), mrSparkzz.nextMessage());
        assertEquals(100, store.getBalance());
        // TODO: assertEquals(0, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - query price");
    }

    @Test
    @DisplayName("Test Sell - main functionality - no store")
    @Order(7)
    void testSellCommand_NoStore() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop sell emerald 2");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_STORE_FOUND, null), mrSparkzz.nextMessage());
        assertEquals(100, store.getBalance());
        // TODO: assertEquals(0, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - no store");
    }
}
