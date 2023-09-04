package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Core;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.Map;

import static net.sparkzz.shops.TestHelper.*;
import static net.sparkzz.shops.util.Notifier.CipherKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Buy Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BuyCommandTest {
    private static final ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

    private static PlayerMock mrSparkzz, player2;
    private static Store store;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST BUY COMMAND ]==");
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
    void setUpBuyCommand() {
        Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().clear();
        Store.getDefaultStore(mrSparkzz.getWorld()).get().addItem(emeralds.getType(), emeralds.getAmount(), -1, 2D, 1.5D);
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
    }

    @AfterEach
    void tearDownEach() {
        mrSparkzz.getInventory().clear();
    }

    @Test
    @DisplayName("Test Buy - permissions")
    @Order(1)
    void testBuyCommand_Permissions() {
        performCommand(player2, "shop buy emerald 1");
        assertEquals(Notifier.compose(NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("buy command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Buy - main functionality")
    @Order(2)
    void testBuyCommand() {
        performCommand(mrSparkzz, "shop buy emerald 12");
        assertEquals(Notifier.compose(BUY_SUCCESS, Map.of("quantity", 12, "material", Material.EMERALD, "cost", 24)), mrSparkzz.nextMessage());
        assertEquals(24, store.getBalance());
        assertEquals(26, Core.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test");
    }

    @Test
    @DisplayName("Test Buy - main functionality - below minimum amount")
    @Order(3)
    void testBuyCommand_BelowMinimum() {
        performCommand(mrSparkzz, "shop buy emerald -1");
        assertEquals(Notifier.compose(INVALID_QUANTITY, Collections.singletonMap("quantity", -1)), mrSparkzz.nextMessage());
        assertEquals(0, store.getBalance());
        // TODO: assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - below minimum amount");
    }

    @Test
    @DisplayName("Test Buy - main functionality - above maximum amount")
    @Order(4)
    void testBuyCommand_AboveMaximum() {
        performCommand(mrSparkzz, "shop buy emerald 2305");
        assertEquals(Notifier.compose(INVALID_QUANTITY, Collections.singletonMap("quantity", 2305)), mrSparkzz.nextMessage());
        assertEquals(0, store.getBalance());
        // TODO: assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - above maximum amount");
    }

    @Test
    @DisplayName("Test Buy - main functionality - invalid material")
    @Order(4)
    void testBuyCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop buy emeral 10");
        assertEquals(Notifier.compose(INVALID_MATERIAL, Collections.singletonMap("material", "emeral")), mrSparkzz.nextMessage());
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        assertEquals(0, store.getBalance());
        // TODO: assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - above maximum amount");
    }

    @Test
    @DisplayName("Test Buy - main functionality - query price")
    @Order(5)
    void testBuyCommand_QueryPrice() {
        performCommand(mrSparkzz, "shop buy emerald");
        assertEquals(Notifier.compose(PRICE, Collections.singletonMap("cost", 2D)), mrSparkzz.nextMessage());
        assertEquals(0, store.getBalance());
        // TODO: assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - query price");
    }

    @Test
    @DisplayName("Test Buy - main functionality - no store")
    @Order(6)
    void testBuyCommand_NoStore() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop buy emerald 2");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_STORE_FOUND, null), mrSparkzz.nextMessage());
        assertEquals(0, store.getBalance());
        // TODO: assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - no store");
    }
}