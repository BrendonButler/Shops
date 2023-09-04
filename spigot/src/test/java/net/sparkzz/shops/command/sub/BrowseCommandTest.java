package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Browse Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrowseCommandTest {

    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST BROWSE COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();
        World world = server.createWorld(WorldCreator.name("world"));

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);
        loadConfig();

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        mrSparkzz.setLocation(new Location(world, 0, 0, 0));
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        unLoadConfig();
        Store.DEFAULT_STORES.clear();
    }

    @BeforeEach
    void setUpShopItems() {
        Store store;
        Store.setDefaultStore(mrSparkzz.getWorld(), (store = new Store("BetterBuy", mrSparkzz.getUniqueId())));

        store.addItem(Material.EMERALD, 3, 64, 24.5, 12);
        store.addItem(Material.ACACIA_LOG, 2018, -1, 2, 1);
        store.addItem(Material.ITEM_FRAME, 1, 24, 13, 10);
        store.addItem(Material.OBSIDIAN, 100, 4, 2, 1);
        store.addItem(Material.ACACIA_SIGN, 0, -1, 0, 0);
        store.addItem(Material.IRON_AXE, 4, 10, 10, 5);
        store.addItem(Material.COPPER_BLOCK, 10000, -1, 2, 1);
        store.addItem(Material.CHARCOAL, 10, 128, .5, .1);
        store.addItem(Material.BEEF, 1000, 2048, 4, 1.5);
        store.addItem(Material.BUCKET, 2, 12, 10, 2.5);
        store.addItem(Material.SPRUCE_LOG, 40, 64, 4, 2);
        store.addItem(Material.STICK, 12800, -1, 0.25, 0.1);
    }

    @AfterEach
    void tearDownStore() {
        Store.STORES.clear();
    }

    @Test
    @DisplayName("Test Browse - permissions")
    @Order(1)
    void testBrowseShop_Permissions() {
        performCommand(player2, "shop browse");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("browse command permission check");
    }

    @Test
    @DisplayName("Test Browse - main functionality")
    @Order(2)
    void testBrowseShop() {
        performCommand(mrSparkzz, "shop browse");
        assertEquals("""
                §7==[ §3BetterBuy§7 ]==
                §nITEM          | BUY PRICE | SELL PRICE
                §2COPPER_BLOCK  §r: §62.00      §r| §61.00
                §2SPRUCE_LOG    §r: §64.00      §r| §62.00
                §2ACACIA_LOG    §r: §62.00      §r| §61.00
                §2OBSIDIAN      §r: §62.00      §r| §61.00
                §2CHARCOAL      §r: §60.50      §r| §60.10
                §2EMERALD       §r: §624.50     §r| §612.00
                §2IRON_AXE      §r: §610.00     §r| §65.00
                §2STICK         §r: §60.25      §r| §60.10
                §2ACACIA_SIGN   §r: §60.00      §r| §60.00
                §2BUCKET        §r: §610.00     §r| §62.50
                Page 1 of 2""", mrSparkzz.nextMessage());
        printSuccessMessage("browse command test - browse page 1");
    }

    @Test
    @DisplayName("Test Browse - main functionality - invalid shop")
    @Order(2)
    void testBrowse_InvalidShop() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop browse");
        assertEquals("§cYou are not currently in a store!", mrSparkzz.nextMessage());
        printSuccessMessage("browse command test - invalid shop");
    }

    @Test
    @DisplayName("Test Browse - main functionality - second page")
    @Order(4)
    void testBrowseShop_SecondPage() {
        performCommand(mrSparkzz, "shop browse 2");
        assertEquals("""
                §7==[ §3BetterBuy§7 ]==
                §nITEM        | BUY PRICE | SELL PRICE
                §2BEEF        §r: §64.00      §r| §61.50
                §2ITEM_FRAME  §r: §613.00     §r| §610.00
                Page 2 of 2""", mrSparkzz.nextMessage());
        printSuccessMessage("browse command test - browse page 2");
    }

    @Test
    @DisplayName("Test Browse - main functionality - invalid page")
    @Order(5)
    void testBrowseShop_InvalidPage() {
        performCommand(mrSparkzz, "shop browse 3");
        assertEquals("§cInvalid page number!", mrSparkzz.nextMessage());
        printSuccessMessage("browse command test - browse page 3 (invalid page)");
    }
}
