package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.Material;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Update Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UpdateCommandTest {

    private static boolean wasInfStock, wasInfFunds;
    private static PlayerMock mrSparkzz;
    private static ServerMock server;
    private static String oldName;
    private static Store store;

    @BeforeAll
    static void saveOldValues() {
        printMessage("==[ TEST UPDATE COMMAND ]==");
        server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        mrSparkzz = server.addPlayer("MrSparkzz");

        mrSparkzz.setOp(true);
        Shops.setDefaultShop((store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
        store.addItem(Material.EMERALD, 0, -1, 2D, 1.5D);
        oldName = Shops.getDefaultShop().getName();
        wasInfFunds = Shops.getDefaultShop().hasInfiniteFunds();
        wasInfStock = Shops.getDefaultShop().hasInfiniteStock();
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
        Store.STORES.clear();
    }

    @AfterEach
    void resetShop() {
        Shops.getDefaultShop().setName(oldName);
        Shops.getDefaultShop().setInfiniteFunds(wasInfFunds);
        Shops.getDefaultShop().setInfiniteStock(wasInfStock);
    }

    @Test
    @DisplayName("Test Update - main functionality - customer buy price")
    @Order(1)
    void testUpdateCommand_BuyPrice() {
        performCommand(mrSparkzz, "shop update emerald customer-buy-price 5");
        assertEquals("§aYou have successfully updated §6customer-buy-price§a to §65§a in the store!", mrSparkzz.nextMessage());
        assertEquals(5D, store.getAttributes(Material.EMERALD).get("buy").doubleValue());
        printSuccessMessage("update command test - customer buy price");
    }

    @Test
    @DisplayName("Test Update - main functionality - customer sell price")
    @Order(2)
    void testUpdateCommand_SellPrice() {
        performCommand(mrSparkzz, "shop update emerald customer-sell-price 5");
        assertEquals("§aYou have successfully updated §6customer-sell-price§a to §65§a in the store!", mrSparkzz.nextMessage());
        assertEquals(5D, store.getAttributes(Material.EMERALD).get("sell").doubleValue());
        printSuccessMessage("update command test - customer sell price");
    }

    @Test
    @DisplayName("Test Update - main functionality - max quantity")
    @Order(3)
    void testUpdateCommand_MaxQuantity() {
        performCommand(mrSparkzz, "shop update emerald max-quantity 128");
        assertEquals("§aYou have successfully updated §6max-quantity§a to §6128§a in the store!", mrSparkzz.nextMessage());
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
        assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in the store!", GREEN, GOLD, "infinite-funds", GREEN, GOLD, "true", GREEN), mrSparkzz.nextMessage());
        assertTrue(store.hasInfiniteFunds());
        printSuccessMessage("update command test - update infinite funds");
    }

    @Test
    @DisplayName("Test Update - main functionality - inf-stock true")
    @Order(6)
    void testUpdateCommand_InfStock() {
        performCommand(mrSparkzz, "shop update infinite-stock true");
        assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in the store!", GREEN, GOLD, "infinite-stock", GREEN, GOLD, "true", GREEN), mrSparkzz.nextMessage());
        assertTrue(store.hasInfiniteStock());
        printSuccessMessage("update command test - update infinite stock");
    }

    @Test
    @DisplayName("Test Update - main functionality - shop name")
    @Order(7)
    void testUpdateCommand_ShopName() {
        performCommand(mrSparkzz, "shop update shop-name TestShop99");
        assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in the store!", GREEN, GOLD, "shop-name", GREEN, GOLD, "TestShop99", GREEN), mrSparkzz.nextMessage());
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
        store.addItem(Material.BUCKET, 1, 64, 1D, 0.5D);

        performCommand(mrSparkzz, "shop update bucket infinite-quantity true");
        assertEquals("§cPlease ensure there is no stock in the store for this item and try again!", mrSparkzz.nextMessage());
        assertFalse(store.hasInfiniteStock());
        printSuccessMessage("update command test - infinite stock per item allow but has stock");
    }

    @Test
    @DisplayName("Test Update - main functionality - infinite stock per item allow false")
    @Order(13)
    void testUpdateCommand_Permissions_InfStockItemFalse() {
        store.addItem(Material.BUCKET, 1, 64, 1D, 0.5D);

        performCommand(mrSparkzz, "shop update bucket infinite-quantity false");
        assertEquals("§aYou have successfully updated §6infinite-quantity§a to §6false§a in the store!", mrSparkzz.nextMessage());
        assertFalse(store.hasInfiniteStock());
        printSuccessMessage("update command test - infinite stock per item allow false");
    }

    @Test
    @DisplayName("Test Update - main functionality - infinite stock per item allow")
    @Order(14)
    void testUpdateCommand_Permissions_InfStockItem() {
        store.removeItem(Material.BUCKET, store.getAttributes(Material.BUCKET).get("quantity").intValue());

        performCommand(mrSparkzz, "shop update bucket infinite-quantity true");
        assertEquals("§aYou have successfully updated §6infinite-quantity§a to §6true§a in the store!", mrSparkzz.nextMessage());
        assertEquals(-1, store.getAttributes(Material.BUCKET).get("quantity").intValue());
        printSuccessMessage("update command test - infinite stock per item allow");
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
            assertEquals(String.format("%sYou do not have permission to use this command!", RED), player.nextMessage());
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
