package net.sparkzz.shops.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.inventory.ItemStack;
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

import java.util.List;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("InventoryManagementSystem Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryManagementSystemTest {

    private static final ItemStack emeralds = new ItemStack(Material.EMERALD, 64);
    private static final ItemStack snowballs = new ItemStack(Material.SNOWBALL, 14);
    private static PlayerMock mrSparkzz;
    private static ServerMock server;
    private static Store store, secondaryStore;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST IMS ]==");
        server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);
        loadConfig();

        mrSparkzz = server.addPlayer("MrSparkzz");

        mrSparkzz.setOp(true);
        server.createWorld(WorldCreator.name("world"));
        server.createWorld(WorldCreator.name("world_nether"));
        Store.setDefaultStore(mrSparkzz.getWorld(), (store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
        secondaryStore = new Store("SecondaryStore", mrSparkzz.getUniqueId(), new Cuboid(server.getWorld("world"), -20D, -20D, -20D, 20D, 20D, 20D));
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        unLoadConfig();
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
    }

    @BeforeEach
    void setUpIMS() {
        Store.setDefaultStore(null, store);
        store.addItem(emeralds.getType(), emeralds.getAmount(), 128, 1, 1);
    }

    @AfterEach
    void tearDownIMS() {
        store.getItems().clear();
        store.setInfiniteStock(false);
        mrSparkzz.getInventory().clear();
    }

    @Test
    @DisplayName("Test IMS - can insert item")
    @Order(1)
    void testCanInsert() {
        boolean canInsert = InventoryManagementSystem.canInsert(mrSparkzz, emeralds.getType(), emeralds.getAmount());
        assertTrue(canInsert);
        printSuccessMessage("IMS - can insert item");
    }

    @Test
    @DisplayName("Test IMS - can't insert item")
    @Order(2)
    void testCantInsert() {
        mrSparkzz.getInventory().addItem(new ItemStack(Material.EMERALD, 2304));

        boolean canInsert = InventoryManagementSystem.canInsert(mrSparkzz, emeralds.getType(), emeralds.getAmount());
        assertFalse(canInsert);
        printSuccessMessage("IMS - can't insert item");
    }

    @Test
    @DisplayName("Test IMS - can insert all items")
    @Order(3)
    void testCanInsertAll() {
        boolean canInsertAll = InventoryManagementSystem.canInsertAll(mrSparkzz, List.of(emeralds, snowballs));
        assertTrue(canInsertAll);
        printSuccessMessage("IMS - can insert all items");
    }

    @Test
    @DisplayName("Test IMS - can't insert all items")
    @Order(4)
    void testCantInsertAll() {
        mrSparkzz.getInventory().addItem(new ItemStack(Material.EMERALD, 2176), snowballs);

        boolean canInsertAll = InventoryManagementSystem.canInsertAll(mrSparkzz, List.of(emeralds, snowballs));
        assertFalse(canInsertAll);
        printSuccessMessage("IMS - can't insert all items");
    }

    @Test
    @DisplayName("Test IMS - can remove")
    @Order(5)
    void testCanRemove() {
        mrSparkzz.getInventory().addItem(new ItemStack(Material.EMERALD, 2176), snowballs);

        boolean canRemove = InventoryManagementSystem.canRemove(mrSparkzz, emeralds.getType(), emeralds.getAmount());
        assertTrue(canRemove);
        printSuccessMessage("IMS - can't insert all items");
    }

    @Test
    @DisplayName("Test IMS - can't remove item")
    @Order(6)
    void testCantRemove() {
        boolean canRemove = InventoryManagementSystem.canRemove(mrSparkzz, snowballs.getType(), snowballs.getAmount());
        assertFalse(canRemove);
        printSuccessMessage("IMS - can't remove item");
    }

    @Test
    @DisplayName("Test IMS - contains at least")
    @Order(7)
    void testContainsAtLeast() {
        boolean containsAtLeast = InventoryManagementSystem.containsAtLeast(store, emeralds);
        assertTrue(containsAtLeast);
        printSuccessMessage("IMS - contains at least");
    }

    @Test
    @DisplayName("Test IMS - contains at least - infinite stock")
    @Order(8)
    void testContainsAtLeast_InfiniteStock() {
        store.setInfiniteStock(true);

        boolean containsAtLeast = InventoryManagementSystem.containsAtLeast(store, new ItemStack(Material.EMERALD, 65));
        assertTrue(containsAtLeast);
        printSuccessMessage("IMS - contains at least - infinite stock");
    }

    @Test
    @DisplayName("Test IMS - doesn't contain at least")
    @Order(9)
    void testContainsAtLeast_Fail() {
        boolean containsAtLeast = InventoryManagementSystem.containsAtLeast(store, snowballs);
        assertFalse(containsAtLeast);
        printSuccessMessage("IMS - doesn't contain at least");
    }

    @Test
    @DisplayName("Test IMS - count quantity (player)")
    @Order(10)
    void testCountQuantity_Player() {
        mrSparkzz.getInventory().addItem(emeralds);

        int quantity = InventoryManagementSystem.countQuantity(mrSparkzz, emeralds.getType());
        assertEquals(64, quantity);
        printSuccessMessage("IMS - count quantity (player)");
    }

    @Test
    @DisplayName("Test IMS - count quantity (store)")
    @Order(11)
    void testCountQuantity_Store() {
        int quantity = InventoryManagementSystem.countQuantity(store, emeralds.getType());
        assertEquals(64, quantity);
        printSuccessMessage("IMS - count quantity (store)");
    }

    @Test
    @DisplayName("Test IMS - count quantity (store) - item infinite stock")
    @Order(12)
    void testCountQuantity_Store_ItemInfiniteStock() {
        store.addItem(Material.BUCKET, -1);

        int quantity = InventoryManagementSystem.countQuantity(store, Material.BUCKET);
        assertEquals(Integer.MAX_VALUE, quantity);
        printSuccessMessage("IMS - count quantity (store) - item infinite stock");
    }

    @Test
    @DisplayName("Test IMS - get available space (store)")
    @Order(13)
    void testGetAvailableSpace_Store() {
        int quantity = InventoryManagementSystem.getAvailableSpace(store, emeralds.getType());
        assertEquals(64, quantity);
        printSuccessMessage("IMS - get available space (store)");
    }

    @Test
    @DisplayName("Test IMS - get available space (store) - no material in store")
    @Order(14)
    void testGetAvailableSpace_Store_NoMaterialInStore() {
        int quantity = InventoryManagementSystem.getAvailableSpace(store, snowballs.getType());
        assertEquals(0, quantity);
        printSuccessMessage("IMS - get available space (store) - no material in store");
    }

    @Test
    @DisplayName("Test IMS - get available space (store) - no material in store - infinite stock")
    @Order(15)
    void testGetAvailableSpace_Store_NoMaterialInStore_InfiniteStock() {
        store.setInfiniteStock(true);

        int quantity = InventoryManagementSystem.getAvailableSpace(store, snowballs.getType());
        assertEquals(0, quantity);
        printSuccessMessage("IMS - get available space (store) - no material in store - infinite stock");
    }

    @Test
    @DisplayName("Test IMS - get available space (store) - infinite stock")
    @Order(16)
    void testGetAvailableSpace_Store_InfiniteStock() {
        store.setInfiniteStock(true);

        int quantity = InventoryManagementSystem.getAvailableSpace(store, emeralds.getType());
        assertEquals(Integer.MAX_VALUE, quantity);
        printSuccessMessage("IMS - get available space (store) - infinite stock");
    }

    @Test
    @DisplayName("Test IMS - get available space (store) - max quantity negative")
    @Order(17)
    void testGetAvailableSpace_Store_MaxQuantityNegative() {
        store.getAttributes(emeralds.getType()).put("max_quantity", -1);

        int quantity = InventoryManagementSystem.getAvailableSpace(store, emeralds.getType());
        assertEquals(Integer.MAX_VALUE, quantity);
        printSuccessMessage("IMS - get available space (store) - max quantity negative");
    }

    @Test
    @DisplayName("Test IMS - identify store - within secondary store")
    @Order(18)
    void testIdentifyStore_WithinSecondaryStore() {
        Store.setDefaultStore(null, null);
        mrSparkzz.setLocation(new Location(server.getWorld("world"), 0D, 0D, 0D));

        assertEquals(secondaryStore, InventoryManagementSystem.locateCurrentStore(mrSparkzz).orElse(null));
        printSuccessMessage("IMS - identify store - within secondary store");
    }

    @Test
    @DisplayName("Test IMS - identify store - within default store")
    @Order(19)
    void testIdentifyStore_NotWithinSecondaryStore() {
        mrSparkzz.setLocation(new Location(server.getWorld("world"), 21D, 0D, 0D));

        assertEquals(store, InventoryManagementSystem.locateCurrentStore(mrSparkzz).orElse(null));
        printSuccessMessage("IMS - identify store - within default store");
    }

    @Test
    @DisplayName("Test IMS - identify store - not within any store")
    @Order(20)
    void testIdentifyStore_NotWithinAnyStore() {
        mrSparkzz.setLocation(new Location(server.getWorld("not-a-world"), 0D, 0D, 0D));
        Store.DEFAULT_STORES.clear();

        System.out.println(mrSparkzz.getWorld());
        assertNull(InventoryManagementSystem.locateCurrentStore(mrSparkzz).orElse(null));
        printSuccessMessage("IMS - identify store - not within secondary store");
    }

    @Test
    @DisplayName("Test IMS - identify store - inter-global default in another world")
    @Order(21)
    void testIdentifyStore_InterGlobalDefaultInAnotherWorld() {
        mrSparkzz.setLocation(new Location(server.getWorld("world_nether"), 0D, 0D, 0D));

        System.out.println(mrSparkzz.getWorld());
        assertEquals(store, InventoryManagementSystem.locateCurrentStore(mrSparkzz).orElse(null));
        printSuccessMessage("IMS - identify store - not within secondary store");
    }
}
