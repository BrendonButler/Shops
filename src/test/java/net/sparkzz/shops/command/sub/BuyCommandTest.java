package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.RED;
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

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Shops.setDefaultShop((store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @BeforeEach
    void setUpBuyCommand() {
        Shops.getDefaultShop().getItems().clear();
        Shops.getDefaultShop().addItem(emeralds.getType(), emeralds.getAmount(), -1, 2D, 1.5D);
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
    }

    @Test
    @DisplayName("Test Buy - permissions")
    @Order(1)
    void testBuyCommand_Permissions() {
        performCommand(player2, "shop buy emerald 1");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("buy command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Buy - main functionality")
    @Order(2)
    void testBuyCommand() {
        performCommand(mrSparkzz, "shop buy emerald 12");
        assertEquals("§aSuccess! You have purchased §612§a of §6EMERALD§a for §624§a.", mrSparkzz.nextMessage());
        assertEquals(24, store.getBalance());
        assertEquals(26, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test");
    }

    @Test
    @DisplayName("Test Buy - main functionality - below minimum amount")
    @Order(3)
    void testBuyCommand_BelowMinimum() {
        performCommand(mrSparkzz, "shop buy emerald -1");
        assertEquals("§cInvalid quantity (-1)!", mrSparkzz.nextMessage());
        assertEquals(0, store.getBalance());
        // TODO: assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - below minimum amount");
    }

    @Test
    @DisplayName("Test Buy - main functionality - above maximum amount")
    @Order(4)
    void testBuyCommand_AboveMaximum() {
        performCommand(mrSparkzz, "shop buy emerald 2305");
        assertEquals("§cInvalid quantity (2305)!", mrSparkzz.nextMessage());
        assertEquals(0, store.getBalance());
        // TODO: assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - above maximum amount");
    }

    @Test
    @DisplayName("Test Buy - main functionality - invalid material")
    @Order(4)
    void testBuyCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop buy emeral 10");
        assertEquals("§cInvalid material (emeral)!", mrSparkzz.nextMessage());
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
        assertEquals("§9Price: §a2.0", mrSparkzz.nextMessage());
        assertEquals(0, store.getBalance());
        // TODO: assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - query price");
    }
}