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
import static org.junit.jupiter.api.Assertions.assertNull;

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
                Shops.shop.getItems().clear();
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

        /**
         * Test cases for Remove Command
         */
        @Nested
        @DisplayName("Remove Command")
        @Order(4)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class RemoveCommand {
            static ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

            @BeforeAll
            static void setUpRemoveCommand() {
                printMessage("==[ TEST REMOVE COMMAND ]==");
                Shops.shop.getItems().clear();
                Shops.shop.addItem(emeralds.getType(), 0, -1, 2D, 1.5D);
            }

            @Test
            @DisplayName("Test Remove - permissions")
            @Order(1)
            void testRemoveCommand_Permissions() {
                performCommand(player2, "shop remove acacia_log 1");
                assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
                printSuccessMessage("remove command permission check");
            }

            @Test
            @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
            @DisplayName("Test Remove - main functionality - remove 1")
            @Order(2)
            void testRemoveCommand_RemoveOne() {
                Material material = emeralds.getType();
                int quantity = 0;

                performCommand(mrSparkzz, "shop remove emerald 1");
                assertEquals(String.format("%sYou have successfully removed %s%s%s from the shop!", GREEN, GOLD, (quantity > 0) ? String.valueOf(quantity) + GREEN + " of " + GOLD + material : material, GREEN), mrSparkzz.nextMessage());
                assertEquals(63, mrSparkzz.getInventory().getItem(0).getAmount());
                assertEquals(11, Shops.shop.getItems().get(material).get("quantity").intValue());
                printSuccessMessage("remove 1 of type from shop");
            }

            @Test
            @DisplayName("Test Remove - main functionality - remove all")
            @Order(3)
            void testRemoveCommand_RemoveAll() {
                Material material = emeralds.getType();
                int quantity = 0;

                performCommand(mrSparkzz, "shop remove emerald");
                assertEquals(String.format("%sYou have successfully removed %s%s%s from the shop!", GREEN, GOLD, (quantity > 0) ? String.valueOf(quantity) + GREEN + " of " + GOLD + material : material, GREEN), mrSparkzz.nextMessage());
                assertNull(Shops.shop.getItems().get(material));
                printSuccessMessage("remove all of type from shop");
            }

            @Test
            @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
            @DisplayName("Test Remove - material not found in shop")
            @Order(4)
            void testRemoveCommand_NoMaterial() {
                performCommand(mrSparkzz, "shop remove emerald 1");
                assertEquals(String.format("%sThis material doesn't currently exist in the shop, use `/shop add %s` to add this item", RED, Material.EMERALD), mrSparkzz.nextMessage());

                printSuccessMessage("remove command - material doesn't exist");
            }
        }

        /**
         * Test cases for Deposit Command
         */
        @Nested
        @DisplayName("Deposit Command")
        @Order(5)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class DepositCommand {

            @BeforeEach
            void setUpDepositCommand() {
                printMessage("==[ TEST DEPOSIT COMMAND ]==");
                // TODO: Shops.econ.depositPlayer(mrSparkzz, 150);
                Shops.shop.addFunds(25);
            }

            @Test
            @DisplayName("Test Deposit - permissions")
            @Order(1)
            void testWithdrawCommand_Permissions() {
                performCommand(player2, "shop deposit 100");
                assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
                printSuccessMessage("deposit command permission check");
            }

            @Test
            @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
            @DisplayName("Test Deposit - main functionality - deposit 100")
            @Order(2)
            void testDepositCommand() {
                double amount = 100;

                performCommand(mrSparkzz, "shop deposit " + amount);
                assertEquals(String.format("%sYou have successfully deposited %s%s%s to the shop!", GREEN, GOLD, amount, GREEN), mrSparkzz.nextMessage());
                assertEquals(125, Shops.shop.getBalance());
                assertEquals(50, Shops.econ.getBalance(mrSparkzz));
            }
        }

        @Nested
        @DisplayName("Withdraw Command")
        @Order(6)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class WithdrawCommand {

            @BeforeEach
            void setUpWithdrawCommand() {
                printMessage("==[ TEST WITHDRAW COMMAND ]==");
                // TODO: Shops.econ.depositPlayer(mrSparkzz, 50);
                Shops.shop.addFunds(125);
            }

            @Test
            @DisplayName("Test Withdraw - permissions")
            @Order(1)
            void testWithdrawCommand_Permissions() {
                performCommand(player2, "shop withdraw 100");
                assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
                printSuccessMessage("deposit command permission check");
            }

            @Test
            @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
            @DisplayName("Test Withdraw - main functionality - withdraw 100")
            @Order(2)
            void testWithdrawCommand() {
                double amount = 100;

                performCommand(mrSparkzz, "shop withdraw " + amount);
                assertEquals(String.format("%sYou have successfully withdrawn %s%s%s from the shop!", GREEN, GOLD, amount, GREEN), mrSparkzz.nextMessage());
                assertEquals(25, Shops.shop.getBalance());
                assertEquals(150, Shops.econ.getBalance(mrSparkzz));
            }
        }

        @Nested
        @DisplayName("Buy Command")
        @Order(7)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class BuyCommand {
            static ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

            @BeforeEach
            void setUpBuyCommand() {
                printMessage("==[ TEST BUY COMMAND ]==");
                Shops.shop.getItems().clear();
                Shops.shop.addItem(emeralds.getType(), emeralds.getAmount(), -1, 2D, 1.5D);
                // TODO: Shops.econ.depositPlayer(mrSparkzz, 50);
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
            @DisplayName("Test Buy - main functionality - buy 100")
            @Order(2)
            void testBuyCommand() {
                int quantity = 1;

                performCommand(mrSparkzz, "shop buy " + quantity);
                assertEquals(String.format("%sYou have successfully deposited %s%s%s to the shop!", GREEN, GOLD, quantity, GREEN), mrSparkzz.nextMessage());
                assertEquals(25, Shops.shop.getBalance());
                assertEquals(150, Shops.econ.getBalance(mrSparkzz));
            }
        }

        @Nested
        @DisplayName("Sell Command")
        @Order(8)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class SellCommand {
            static ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

            @BeforeEach
            void setUpSellCommand() {
                printMessage("==[ TEST SELL COMMAND ]==");
                Shops.shop.getItems().clear();
                Shops.shop.addItem(emeralds.getType(), 0, -1, 2D, 1.5D);
                Shops.shop.addFunds(100);
                mrSparkzz.getInventory().addItem(emeralds);
                // TODO: Shops.econ.depositPlayer(mrSparkzz, 50);
            }

            @Test
            @DisplayName("Test Sell - permissions")
            @Order(1)
            void testSellCommand_Permissions() {
                performCommand(player2, "shop sell emerald 1");
                assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
                printSuccessMessage("buy command permission check");
            }

            @Test
            @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
            @DisplayName("Test Sell - main functionality - sell 1")
            @Order(2)
            void testSellCommand() {
                Material material = emeralds.getType();
                int quantity = 1;
                double price = Shops.shop.getSellPrice(material);

                performCommand(mrSparkzz, "shop sell emerald " + quantity);
                assertEquals(String.format("%sSuccess! You have sold %s%s%s of %s%s%s for %s$%.2f%s.",
                        GREEN, GOLD, quantity, GREEN, GOLD, material, GREEN, GOLD, price * quantity, GREEN), mrSparkzz.nextMessage());
                assertEquals(25, Shops.shop.getBalance());
                assertEquals(150, Shops.econ.getBalance(mrSparkzz));
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