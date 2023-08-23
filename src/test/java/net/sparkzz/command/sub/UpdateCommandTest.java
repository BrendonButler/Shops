package net.sparkzz.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.util.Cuboid;
import net.sparkzz.util.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GREEN;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Update Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UpdateCommandTest {

    private static Cuboid defaultLocation, cuboidLocation, cuboidLocationNether;
    private static PlayerMock mrSparkzz;
    private static ServerMock server;
    private static Store store;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST UPDATE COMMAND ]==");
        server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);
        loadConfig();

        mrSparkzz = server.addPlayer("MrSparkzz");
        mrSparkzz.setOp(true);
        server.createWorld(WorldCreator.name("world"));
        server.createWorld(WorldCreator.name("world-nether"));
        defaultLocation = new Cuboid(server.getWorld("world"), 0D, 0D, 0D, 50D, 50D, 50D);
        cuboidLocation = new Cuboid(server.getWorld("world"), 10D, 20D, 30D, 40D, 50D, 60D);
        cuboidLocationNether = new Cuboid(server.getWorld("world-nether"), 10D, 20D, 30D, 40D, 50D, 60D);
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        unLoadConfig();
        Store.DEFAULT_STORES.clear();
    }

    @BeforeEach
    void setupShop() {
        mrSparkzz.setLocation(new Location(server.getWorld("world"), 0D, 0D, 0D));
        store = new Store("BetterBuy", mrSparkzz.getUniqueId(), new Cuboid(server.getWorld("world"), 0D, 0D, 0D, 50D, 50D, 50D));
        store.addItem(Material.EMERALD, 0, -1, 2D, 1.5D);
        store.addItem(Material.BUCKET, 1, 64, 1D, 0.5D);
    }

    @AfterEach
    void resetShop() {
        mrSparkzz.nextMessage();
        Store.STORES.clear();
    }

    @Test
    @DisplayName("Test Update - main functionality - customer buy price")
    @Order(1)
    void testUpdateCommand_BuyPrice() {
        performCommand(mrSparkzz, "shop update emerald customer-buy-price 5");
        assertEquals("§aYou have successfully updated §6customer-buy-price§a to §65§a in BetterBuy!", mrSparkzz.nextMessage());
        assertEquals(5D, store.getAttributes(Material.EMERALD).get("buy").doubleValue());
        printSuccessMessage("update command test - customer buy price");
    }

    @Test
    @DisplayName("Test Update - main functionality - customer sell price")
    @Order(2)
    void testUpdateCommand_SellPrice() {
        performCommand(mrSparkzz, "shop update emerald customer-sell-price 5");
        assertEquals("§aYou have successfully updated §6customer-sell-price§a to §65§a in BetterBuy!", mrSparkzz.nextMessage());
        assertEquals(5D, store.getAttributes(Material.EMERALD).get("sell").doubleValue());
        printSuccessMessage("update command test - customer sell price");
    }

    @Test
    @DisplayName("Test Update - main functionality - max quantity")
    @Order(3)
    void testUpdateCommand_MaxQuantity() {
        performCommand(mrSparkzz, "shop update emerald max-quantity 128");
        assertEquals("§aYou have successfully updated §6max-quantity§a to §6128§a in BetterBuy!", mrSparkzz.nextMessage());
        assertEquals(128, store.getAttributes(Material.EMERALD).get("max_quantity").intValue());
        printSuccessMessage("update command test - max quantity");
    }

    @Test
    @DisplayName("Test Update - main functionality - too few args")
    @Order(4)
    void testUpdateCommand_TooFewArgs() {
        performCommand(mrSparkzz, "shop update");
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        printSuccessMessage("update command test - too few args");
    }

    @Test
    @DisplayName("Test Update - main functionality - infinite-funds true")
    @Order(5)
    void testUpdateCommand_InfFunds() {
        performCommand(mrSparkzz, "shop update infinite-funds true");
        assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in BetterBuy!", GREEN, GOLD, "infinite-funds", GREEN, GOLD, "true", GREEN), mrSparkzz.nextMessage());
        assertTrue(store.hasInfiniteFunds());
        printSuccessMessage("update command test - update infinite funds");
    }

    @Test
    @DisplayName("Test Update - main functionality - inf-stock true")
    @Order(6)
    void testUpdateCommand_InfStock() {
        performCommand(mrSparkzz, "shop update infinite-stock true");
        assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in BetterBuy!", GREEN, GOLD, "infinite-stock", GREEN, GOLD, "true", GREEN), mrSparkzz.nextMessage());
        assertTrue(store.hasInfiniteStock());
        printSuccessMessage("update command test - update infinite stock");
    }

    @Test
    @DisplayName("Test Update - main functionality - shop name")
    @Order(7)
    void testUpdateCommand_ShopName() {
        performCommand(mrSparkzz, "shop update shop-name TestShop99");
        assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in TestShop99!", GREEN, GOLD, "shop-name", GREEN, GOLD, "TestShop99", GREEN), mrSparkzz.nextMessage());
        assertEquals("TestShop99", store.getName());
        printSuccessMessage("update command test - update shop name");
    }

    @Test
    @DisplayName("Test Update - main functionality - invalid material")
    @Order(8)
    void testUpdateCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop update emeral customer-buy-price 10");
        assertEquals("§cInvalid material (emeral)!", mrSparkzz.nextMessage());
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        printSuccessMessage("update command test - invalid material");
    }

    @Test
    @DisplayName("Test Update - main functionality - material not in shop")
    @Order(9)
    void testUpdateCommand_MaterialNotInShop() {
        performCommand(mrSparkzz, "shop update diamond customer-buy-price 10");
        assertEquals("§cThis material doesn't currently exist in the store, use `/shop add diamond` to add this item", mrSparkzz.nextMessage());
        printSuccessMessage("update command test - material not in shop");
    }

    @Test
    @DisplayName("Test Update - main functionality - invalid option")
    @Order(10)
    void testUpdateCommand_InvalidOption() {
        performCommand(mrSparkzz, "shop update invalid-option true");
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        printSuccessMessage("update command test - invalid option");
    }

    @Test
    @DisplayName("Test Update - main functionality - invalid option for material")
    @Order(11)
    void testUpdateCommand_InvalidOptionMaterial() {
        performCommand(mrSparkzz, "shop update emerald invalid-option true");
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        printSuccessMessage("update command test - invalid option for material");
    }

    @Test
    @DisplayName("Test Update - main functionality - infinite stock per item allow but has stock")
    @Order(12)
    void testUpdateCommand_Permissions_InfStockItemHasStock() {
        performCommand(mrSparkzz, "shop update bucket infinite-quantity true");
        assertEquals("§cPlease ensure there is no stock in the store for this item and try again!", mrSparkzz.nextMessage());
        assertFalse(store.hasInfiniteStock());
        printSuccessMessage("update command test - infinite stock per item allow but has stock");
    }

    @Test
    @DisplayName("Test Update - main functionality - infinite stock per item allow false")
    @Order(13)
    void testUpdateCommand_Permissions_InfStockItemFalse() {
        performCommand(mrSparkzz, "shop update bucket infinite-quantity false");
        assertEquals("§aYou have successfully updated §6infinite-quantity§a to §6false§a in BetterBuy!", mrSparkzz.nextMessage());
        assertFalse(store.hasInfiniteStock());
        printSuccessMessage("update command test - infinite stock per item allow false");
    }

    @Test
    @DisplayName("Test Update - main functionality - infinite stock per item allow")
    @Order(14)
    void testUpdateCommand_Permissions_InfStockItem() {
        store.removeItem(Material.BUCKET, store.getAttributes(Material.BUCKET).get("quantity").intValue());

        performCommand(mrSparkzz, "shop update bucket infinite-quantity true");
        assertEquals("§aYou have successfully updated §6infinite-quantity§a to §6true§a in BetterBuy!", mrSparkzz.nextMessage());
        assertEquals(-1, store.getAttributes(Material.BUCKET).get("quantity").intValue());
        printSuccessMessage("update command test - infinite stock per item allow");
    }

    @Test
    @DisplayName("Test Update - main functionality - location")
    @Order(15)
    void testUpdateCommand_Location() {
        performCommand(mrSparkzz, "shop update location 10 20 30 40 50 60");
        assertEquals("§aYou have successfully updated the location of BetterBuy to (10.0, 20.0, 30.0) (40.0, 50.0, 60.0) in world!", mrSparkzz.nextMessage());
        assertEquals(cuboidLocation, store.getCuboidLocation());
        printSuccessMessage("update command test - location");
    }

    @Test
    @DisplayName("Test Update - main functionality - location with store")
    @Order(16)
    void testUpdateCommand_Location_WithStore() {
        performCommand(mrSparkzz, "shop update location BetterBuy 10 20 30 40 50 60");
        assertEquals("§aYou have successfully updated the location of BetterBuy to (10.0, 20.0, 30.0) (40.0, 50.0, 60.0) in world!", mrSparkzz.nextMessage());
        assertEquals(cuboidLocation, store.getCuboidLocation());
        printSuccessMessage("update command test - location with store");
    }

    @Test
    @DisplayName("Test Update - main functionality - location with world")
    @Order(17)
    void testUpdateCommand_Location_WithWorld() {
        performCommand(mrSparkzz, "shop update location world-nether 10 20 30 40 50 60");
        assertEquals("§aYou have successfully updated the location of BetterBuy to (10.0, 20.0, 30.0) (40.0, 50.0, 60.0) in world-nether!", mrSparkzz.nextMessage());
        assertEquals(cuboidLocationNether, store.getCuboidLocation());
        printSuccessMessage("update command test - location with world");
    }

    @Test
    @DisplayName("Test Update - main functionality - location with store and world")
    @Order(18)
    void testUpdateCommand_Location_WithStoreAndWorld() {
        performCommand(mrSparkzz, "shop update location BetterBuy world-nether 10 20 30 40 50 60");
        assertEquals("§aYou have successfully updated the location of BetterBuy to (10.0, 20.0, 30.0) (40.0, 50.0, 60.0) in world-nether!", mrSparkzz.nextMessage());
        assertEquals(cuboidLocationNether, store.getCuboidLocation());
        printSuccessMessage("update command test - location with store and world");
    }

    @Test
    @DisplayName("Test Update - main functionality - location with null world")
    @Order(19)
    void testUpdateCommand_Location_WithNullWorld() {
        performCommand(mrSparkzz, "shop update location world-the-start 10 20 30 40 50 60");
        assertEquals("§cCould not find world (world-the-start)!", mrSparkzz.nextMessage());
        assertEquals(defaultLocation, store.getCuboidLocation());
        printSuccessMessage("update command test - location with null world");
    }

    @Test
    @DisplayName("Test Update - main functionality - location with store and null world")
    @Order(20)
    void testUpdateCommand_Location_WithStoreAndNullWorld() {
        performCommand(mrSparkzz, "shop update location BetterBuy world-the-start 10 20 30 40 50 60");
        assertEquals("§cCould not find world (world-the-start)!", mrSparkzz.nextMessage());
        assertEquals(defaultLocation, store.getCuboidLocation());
        printSuccessMessage("update command test - location with store and null world");
    }

    @Test
    @DisplayName("Test Update - main functionality - location with world and invalid store")
    @Order(23)
    void testUpdateCommand_Location_WithWorldAndInvalidStore() {
        performCommand(mrSparkzz, "shop update location DiscountPlus world 10 20 30 40 50 60");
        assertEquals("§cCould not find a store with the name and/or UUID of: §6DiscountPlus§c!", mrSparkzz.nextMessage());
        assertEquals(defaultLocation, store.getCuboidLocation());
        printSuccessMessage("update command test - location with world and invalid store");
    }

    @Test
    @DisplayName("Test Update - main functionality - player not in a store")
    @Order(24)
    void testUpdateCommand_Location_PlayerNotInStore() {
        mrSparkzz.setLocation(new Location(Bukkit.getWorld("world-nether"), 0D, 0D, 0D));
        performCommand(mrSparkzz, "shop update location 10 20 30 40 50 60");
        assertEquals("§cYou are not currently in a store!", mrSparkzz.nextMessage());
        printSuccessMessage("update command test - player not in a store");
    }

    @Test
    @DisplayName("Test Update - main functionality - location update - player not in a store")
    @Order(25)
    void testUpdateCommand_Location_PlayerNotInStore_2() {
        mrSparkzz.setLocation(new Location(Bukkit.getWorld("world-nether"), 0D, 0D, 0D));
        performCommand(mrSparkzz, "shop update location world 10 20 30 40 50 60");
        assertEquals("§cYou are not currently in a store!", mrSparkzz.nextMessage());
        printSuccessMessage("update command test - player not in a store");
    }

    @Test
    @DisplayName("Test Update - main functionality - location update - player not in a store")
    @Order(26)
    void testUpdateCommand_PlayerNotInStore() {
        mrSparkzz.setLocation(new Location(Bukkit.getWorld("world-nether"), 0D, 0D, 0D));
        performCommand(mrSparkzz, "shop update infinite-stock true");
        assertEquals("§cYou are not currently in a store!", mrSparkzz.nextMessage());
        printSuccessMessage("update command test - player not in a store");
    }

    @Nested
    @DisplayName("Update Permissions Test")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PermissionsTest {

        private static boolean cmdUse = true;
        private static final boolean infFunds = false;
        private static final boolean infStock = false;
        private static PlayerMock player;

        @BeforeAll
        static void setUp() {
            player = new PlayerMock(server, "Player999") {
                @Override
                public boolean hasPermission(@NotNull String name) {
                    return switch(name) {
                        case "shops.cmd.update" -> cmdUse;
                        case "shops.update.inf-funds" -> infFunds;
                        case "shops.update.inf-stock" -> infStock;
                        default -> true;
                    };
                }
            };
        }

        @AfterEach
        void tearDownPermissions() {
            cmdUse = true;
        }

        @Test
        @DisplayName("Test Update - permissions")
        @Order(1)
        void testUpdateCommand_Permissions() {
            cmdUse = false;
            performCommand(player, "shop update shop-name TestShop99");
            assertEquals(Notifier.compose(Notifier.CipherKey.NO_PERMS_CMD, null), player.nextMessage());
            printSuccessMessage("update command permission check");
        }

        @Test
        @DisplayName("Test Update - permissions - infinite funds deny")
        @Order(2)
        void testUpdateCommand_Permissions_InfFunds() {
            performCommand(player, "shop update infinite-funds true");
            assertEquals("§cYou do not have permission to set infinite funds in your store!", player.nextMessage());
            assertFalse(store.hasInfiniteFunds());
            printSuccessMessage("update command test - infinite funds deny");
        }

        @Test
        @DisplayName("Test Update - permissions - infinite stock deny")
        @Order(3)
        void testUpdateCommand_Permissions_InfStock() {
            performCommand(player, "shop update infinite-stock true");
            assertEquals("§cYou do not have permission to set infinite stock in your store!", player.nextMessage());
            assertFalse(store.hasInfiniteStock());
            printSuccessMessage("update command test - infinite stock deny");
        }

        @Test
        @DisplayName("Test Update - permissions - infinite stock per item deny")
        @Order(4)
        void testUpdateCommand_Permissions_InfStockItem() {
            performCommand(player, "shop update emerald infinite-quantity true");
            assertEquals("§cYou do not have permission to set infinite stock in your store!", player.nextMessage());
            assertFalse(store.hasInfiniteStock());
            printSuccessMessage("update command test - infinite stock per item deny");
        }
    }
}
