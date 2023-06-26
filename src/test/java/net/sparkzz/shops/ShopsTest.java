package net.sparkzz.shops;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.*;

import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShopsTest {
    private static final String passed = "\u001B[32m[Test] Passed ";
    private static ServerMock server;
    private static Shops plugin;
    private static PlayerMock mrSparkzz, player2, shopper;

    @BeforeAll
    static void setUp() {
        // Start the mock server
        server = MockBukkit.mock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));

        // Load dependencies
        plugin = MockBukkit.load(Shops.class);
        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();
        shopper = server.addPlayer("Shopper #1");

        mrSparkzz.setOp(true);

        // Set up store instances
        Shops.shop = new Store("BetterBuy", mrSparkzz.getUniqueId());
        new Store("DollHairStore", player2.getUniqueId());
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    /**
     * Test cases for Commands
     */
    @Nested
    @DisplayName("Commands")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class Commands {

        /**
         * Test cases for Create Command
         */
        @Nested
        @DisplayName("Create Command")
        @Order(1)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class CreateCommand {

            @BeforeAll
            static void setUp() {
                printMessage("==[ TEST CREATE COMMAND ]==");
            }

            @Test
            @DisplayName("Test Create - permissions")
            @Order(1)
            void testCreateShop_Permissions() {
                performCommand(player2, "shop create TestShop");
                assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
                printSuccessMessage("create command permission check");
            }

            @Test
            @DisplayName("Test Create - main functionality")
            @Order(2)
            void testCreateShop() {
                // TODO: create MockPermissions to add specific permissions to a player mock
                performCommand(mrSparkzz, "shop create TestShop");
                assertEquals(String.format("%sYou have successfully created %s%s%s!", GREEN, GOLD, "TestShop", GREEN), mrSparkzz.nextMessage());
                printSuccessMessage("creation of TestShop");
            }
        }

        /**
         * Test cases for Delete Command
         */
        @Nested
        @DisplayName("Delete Command")
        @Order(2)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class DeleteCommand {

            @BeforeAll
            static void setUp() {
                printMessage("==[ TEST DELETE COMMAND ]==");
            }

            @Test
            @DisplayName("Test Delete - permissions")
            @Order(1)
            void testDeleteShop_Permissions() {
                performCommand(player2, "shop delete DollHairStore");
                assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
                printSuccessMessage("delete command permission check");
            }

            @Test
            @DisplayName("Test Delete - main functionality")
            @Order(2)
            void testDeleteShop() {
                performCommand(mrSparkzz, "shop delete DollHairStore");
                assertEquals(String.format("%sYou have successfully deleted %s%s%s!", GREEN, GOLD, "DollHairStore", GREEN), mrSparkzz.nextMessage());
                printSuccessMessage("deletion of DollHairStore");
            }

            @Test
            @DisplayName("Test Delete - duplicate Store(s)")
            @Order(3)
            void testDeleteShop_DuplicateShops() {
                new Store("TestDuplicate");
                new Store("TestDuplicate");

                performCommand(mrSparkzz, "shop delete TestDuplicate");
                assertEquals(String.format("%sMultiple shops matched, please specify the shop's UUID!", RED), mrSparkzz.nextMessage());
                printSuccessMessage("duplicate shop identification on deletion");
            }

            @Test
            @DisplayName("Test Delete - store not found")
            @Order(4)
            void testDeleteShop_NotFound() {
                performCommand(mrSparkzz, "shop delete TestShop2");
                assertEquals(String.format("%sCould not find a store with the name and/or UUID of: %s%s%s!", RED, GOLD, "TestShop2", RED), mrSparkzz.nextMessage());
                printSuccessMessage("no shop identification on deletion");
            }
        }

        /**
         * Test cases for Add Command
         */
        @Nested
        @DisplayName("Add Command")
        @Order(3)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class AddCommand {
            static ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

            @BeforeAll
            static void setUpAddCommand() {
                printMessage("==[ TEST ADD COMMAND ]==");
                Shops.shop.addItem(emeralds.getType(), 10, -1, 2D, 1.5D);
                Shops.shop.addFunds(100);
            }

            @BeforeEach
            void setUpEach() {
                mrSparkzz.getInventory().addItem(emeralds);
            }

            @AfterEach
            void tearDownEach() {
                mrSparkzz.getInventory().clear();
            }

            @Test
            @DisplayName("Test Add - permissions")
            @Order(1)
            void testAddCommand_Permissions() {
                performCommand(player2, "shop add acacia_log 1");
                assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
                printSuccessMessage("add command permission check");
            }

            @Test
            @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
            @DisplayName("Test Add - main functionality - add 1")
            @Order(2)
            void testAddCommand_AddOne() {
                Material material = emeralds.getType();
                int quantity = emeralds.getAmount();

                performCommand(mrSparkzz, "shop add emerald 1");
                assertEquals(String.format("%sYou have successfully added %s%s%s to the shop!", GREEN, GOLD, (quantity > 0) ? String.valueOf(quantity) + GREEN + " of " + GOLD + material : material, GREEN), mrSparkzz.nextMessage());
                assertEquals(63, mrSparkzz.getInventory().getItem(0).getAmount());
                assertEquals(11, Shops.shop.getItems().get(material).get("quantity").intValue());
            }

            @Test
            @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
            @DisplayName("Test Add - main functionality - add all")
            @Order(3)
            void testAddCommand_AddAll() {

            }

            @Test
            @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
            @DisplayName("Test Add - material not found in shop")
            @Order(4)
            void testAddCommand_NoMaterial() {
                mrSparkzz.getInventory().addItem(new ItemStack(Material.EMERALD, 64));
                performCommand(mrSparkzz, "shop add emerald 1");
                assertEquals(String.format("%sThis material doesn't currently exist in the shop, use `/shop add %s` to add this item", RED, Material.EMERALD), mrSparkzz.nextMessage());

                printSuccessMessage("add command - material doesn't exist");
            }
        }
    }

    private boolean performCommand(CommandSender sender, String message) {
        return server.dispatchCommand(sender, message);
    }

    private static void printSuccessMessage(String message) {
        plugin.getLogger().info(passed + message + "\u001B[0m");
    }

    private static void printMessage(String message) {
        plugin.getLogger().info("\u001B[33m" + message + "\u001B[0m");
    }
}