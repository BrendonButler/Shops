package net.sparkzz.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.*;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
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

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Store.setDefaultStore((store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
        Store.setDefaultStore(null);
        Store.STORES.clear();
    }

    @BeforeEach
    void setUpSellCommand() {
        Store.getDefaultStore().addItem(emeralds.getType(), 0, -1, 2D, 1.5D);
        Store.getDefaultStore().setBalance(100);
        mrSparkzz.getInventory().addItem(emeralds);
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
    }

    @AfterEach
    void tearDownShop() {
        Store.getDefaultStore().getItems().clear();
    }

    @Test
    @DisplayName("Test Sell - permissions")
    @Order(1)
    void testSellCommand_Permissions() {
        performCommand(player2, "shop sell emerald 1");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("sell command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Sell - main functionality - sell 1")
    @Order(2)
    void testSellCommand() {
        Material material = emeralds.getType();
        int quantity = 1;
        double price = Store.getDefaultStore().getSellPrice(material);

        performCommand(mrSparkzz, "shop sell emerald " + quantity);
        assertEquals(String.format("%sSuccess! You have sold %s%s%s of %s%s%s for %s$%.2f%s.",
                GREEN, GOLD, quantity, GREEN, GOLD, material, GREEN, GOLD, price * quantity, GREEN), mrSparkzz.nextMessage());
        assertEquals(25, Store.getDefaultStore().getBalance());
        assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test");
    }

    @Test
    @DisplayName("Test Sell - main functionality - below minimum amount")
    @Order(3)
    void testSellCommand_BelowMinimum() {
        performCommand(mrSparkzz, "shop sell emerald -1");
        assertEquals("§cInvalid quantity (-1)!", mrSparkzz.nextMessage());
        assertEquals(100, store.getBalance());
        // TODO: assertEquals(0, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - below minimum amount");
    }

    @Test
    @DisplayName("Test Sell - main functionality - above maximum amount")
    @Order(4)
    void testSellCommand_AboveMaximum() {
        performCommand(mrSparkzz, "shop sell emerald 2305");
        assertEquals("§cInvalid quantity (2305)!", mrSparkzz.nextMessage());
        assertEquals(100, store.getBalance());
        // TODO: assertEquals(0, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - above maximum amount");
    }

    @Test
    @DisplayName("Test Sell - main functionality - invalid material")
    @Order(5)
    void testSellCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop sell emeral 1");
        assertEquals("§cInvalid material (emeral)!", mrSparkzz.nextMessage());
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
        assertEquals("§9Price: §a1.5", mrSparkzz.nextMessage());
        assertEquals(100, store.getBalance());
        // TODO: assertEquals(0, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - query price");
    }
}
