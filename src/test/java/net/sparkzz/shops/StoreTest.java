package net.sparkzz.shops;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.util.Cuboid;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.printMessage;
import static net.sparkzz.shops.TestHelper.printSuccessMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Entrance Listener")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreTest {

    private static ServerMock server;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST ENTRANCE LISTENER ]==");
        server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        Shops.setMockServer(server);

        Store.setDefaultStore(new Store("BetterBuy"));
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @AfterEach
    void reset() {
        Store.STORES.clear();
        Store.setDefaultStore(new Store("BetterBuy"));
    }

    @Test
    @DisplayName("Test get buy price - material not in store")
    @Order(1)
    void testGetBuyPrice_MaterialNotInStore() {
        assertEquals(-1D, Store.getDefaultStore().getBuyPrice(Material.BEEF));
        printSuccessMessage("get buy price test - material not in store");
    }

    @Test
    @DisplayName("Test get sell price - material not in shop")
    @Order(2)
    void testGetSellPrice_MaterialNotInStore() {
        assertEquals(-1D, Store.getDefaultStore().getSellPrice(Material.BEEF));
        printSuccessMessage("get sell price test - material not in store");
    }

    @Test
    @DisplayName("Test removing funds - more than store balance")
    @Order(3)
    void testRemoveFunds_MoreThanStoreBalance() {
        Store.getDefaultStore().removeFunds(10D);
        assertEquals(0D, Store.getDefaultStore().getBalance());
        printSuccessMessage("remove funds - more than stpre balance");
    }

    @Test
    @DisplayName("Test removing funds")
    @Order(4)
    void testRemoveFunds() {
        Store.getDefaultStore().setBalance(15.52D);
        Store.getDefaultStore().removeFunds(10D);
        assertEquals(5.52D, Store.getDefaultStore().getBalance());
        printSuccessMessage("remove funds");
    }

    @Test
    @DisplayName("Test removing item stack")
    @Order(5)
    void testRemoveItemStack() {
        Store.getDefaultStore().addItem(new ItemStack(Material.SNOWBALL, 20));
        Store.getDefaultStore().removeItem(new ItemStack(Material.SNOWBALL, 15));
        assertEquals(5, Store.getDefaultStore().getAttributes(Material.SNOWBALL).get("quantity"));
        printSuccessMessage("remove item stack");
    }

    @Test
    @DisplayName("Test setting cuboid location")
    @Order(6)
    void testSetCuboidLocation() {
        World world = server.createWorld(WorldCreator.name("world"));
        Cuboid cuboid = new Cuboid(world, 1D, 1D, 1D, 2D, 2D, 2D);
        Store.getDefaultStore().setCuboidLocation(cuboid);
        assertEquals(cuboid, Store.getDefaultStore().getCuboidLocation());
        printSuccessMessage("set cuboid location");
    }
}
