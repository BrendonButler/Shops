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
import static org.junit.jupiter.api.Assertions.*;

class ShopsTest {
    private static ServerMock server;
    private static Shops plugin;
    private static PlayerMock mrSparkzz, player2, shopper;

    @BeforeAll
    static void setUp() {
        // Start the mock server
        server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));

        // Load dependencies
        plugin = MockBukkit.load(Shops.class);
        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();
        shopper = server.addPlayer("Shopper1");

        mrSparkzz.setOp(true);

        // Set up store instances
        Shops.setDefaultShop(new Store("BetterBuy", mrSparkzz.getUniqueId()));
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
        class CreateCommandTest {

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
                printSuccessMessage("create command test - creation of TestShop");
            }
        }

        /**
         * Test cases for Delete Command
         */
        @Nested
        @DisplayName("Delete Command")
        @Order(2)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class DeleteCommandTest {

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
                printSuccessMessage("delete command test - deletion of DollHairStore");
            }

            @Test
            @DisplayName("Test Delete - duplicate Store(s)")
            @Order(3)
            void testDeleteShop_DuplicateShops() {
                new Store("TestDuplicate");
                new Store("TestDuplicate");

                performCommand(mrSparkzz, "shop delete TestDuplicate");
                assertEquals(String.format("%sMultiple shops matched, please specify the shop's UUID!", RED), mrSparkzz.nextMessage());
                printSuccessMessage("delete command test - duplicate shop identification on deletion");
            }

            @Test
            @DisplayName("Test Delete - store not found")
            @Order(4)
            void testDeleteShop_NotFound() {
                performCommand(mrSparkzz, "shop delete TestShop2");
                assertEquals(String.format("%sCould not find a store with the name and/or UUID of: %s%s%s!", RED, GOLD, "TestShop2", RED), mrSparkzz.nextMessage());
                printSuccessMessage("delete command test - no shop identification on deletion");
            }
        }

        /**
         * Test cases for Add Command
         */
        @Nested
        @DisplayName("Add Command")
        @Order(3)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class AddCommandTest {
            static ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

            @BeforeAll
            static void setUpAddCommand() {
                printMessage("==[ TEST ADD COMMAND ]==");
                Shops.getDefaultShop().getItems().clear();
                Shops.getDefaultShop().addItem(emeralds.getType(), 10, -1, 2D, 1.5D);
                Shops.getDefaultShop().addFunds(100);
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
                assertEquals(11, Shops.getDefaultShop().getItems().get(material).get("quantity").intValue());
                printSuccessMessage("add command test - add 1");
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
        class RemoveCommandTest {
            static ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

            @BeforeAll
            static void setUpRemoveCommand() {
                printMessage("==[ TEST REMOVE COMMAND ]==");
                Shops.getDefaultShop().getItems().clear();
                Shops.getDefaultShop().addItem(emeralds.getType(), 0, -1, 2D, 1.5D);
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
                assertEquals(11, Shops.getDefaultShop().getItems().get(material).get("quantity").intValue());
                printSuccessMessage("remove command test - remove 1 of type from shop");
            }

            @Test
            @DisplayName("Test Remove - main functionality - remove all")
            @Order(3)
            void testRemoveCommand_RemoveAll() {
                Material material = emeralds.getType();
                int quantity = 0;

                performCommand(mrSparkzz, "shop remove emerald");
                assertEquals(String.format("%sYou have successfully removed %s%s%s from the shop!", GREEN, GOLD, (quantity > 0) ? String.valueOf(quantity) + GREEN + " of " + GOLD + material : material, GREEN), mrSparkzz.nextMessage());
                assertNull(Shops.getDefaultShop().getItems().get(material));
                printSuccessMessage("remove command test - remove all of type from shop");
            }

            @Test
            @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
            @DisplayName("Test Remove - material not found in shop")
            @Order(4)
            void testRemoveCommand_NoMaterial() {
                performCommand(mrSparkzz, "shop remove emerald 1");
                assertEquals(String.format("%sThis material doesn't currently exist in the shop, use `/shop add %s` to add this item", RED, Material.EMERALD), mrSparkzz.nextMessage());
                printSuccessMessage("remove command test - material doesn't exist");
            }
        }

        /**
         * Test cases for Deposit Command
         */
        @Nested
        @DisplayName("Deposit Command")
        @Order(5)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class DepositCommandTest {

            @BeforeAll
            static void setUp() {
                printMessage("==[ TEST DEPOSIT COMMAND ]==");
            }

            @BeforeEach
            void setUpDepositCommand() {
                // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 150);
                Shops.getDefaultShop().addFunds(25);
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
                assertEquals(125, Shops.getDefaultShop().getBalance());
                assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
                printSuccessMessage("deposit command test");
            }
        }

        @Nested
        @DisplayName("Withdraw Command")
        @Order(6)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class WithdrawCommandTest {

            @BeforeAll
            static void setUp() {
                printMessage("==[ TEST WITHDRAW COMMAND ]==");
            }

            @BeforeEach
            void setUpWithdrawCommand() {
                // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
                Shops.getDefaultShop().addFunds(125);
            }

            @Test
            @DisplayName("Test Withdraw - permissions")
            @Order(1)
            void testWithdrawCommand_Permissions() {
                performCommand(player2, "shop withdraw 100");
                assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
                printSuccessMessage("withdraw command permission check");
            }

            @Test
            @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
            @DisplayName("Test Withdraw - main functionality - withdraw 100")
            @Order(2)
            void testWithdrawCommand() {
                double amount = 100;

                performCommand(mrSparkzz, "shop withdraw " + amount);
                assertEquals(String.format("%sYou have successfully withdrawn %s%s%s from the shop!", GREEN, GOLD, amount, GREEN), mrSparkzz.nextMessage());
                assertEquals(25, Shops.getDefaultShop().getBalance());
                assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
                printSuccessMessage("withdraw command test");
            }
        }

        @Nested
        @DisplayName("Buy Command")
        @Order(7)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class BuyCommandTest {
            static ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

            @BeforeAll
            static void setUp() {
                printMessage("==[ TEST BUY COMMAND ]==");
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
            @DisplayName("Test Buy - main functionality - buy 100")
            @Order(2)
            void testBuyCommand() {
                int quantity = 1;

                performCommand(mrSparkzz, "shop buy " + quantity);
                assertEquals(String.format("%sYou have successfully deposited %s%s%s to the shop!", GREEN, GOLD, quantity, GREEN), mrSparkzz.nextMessage());
                assertEquals(25, Shops.getDefaultShop().getBalance());
                assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
                printSuccessMessage("buy command test");
            }
        }

        @Nested
        @DisplayName("Sell Command")
        @Order(8)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class SellCommandTest {
            static ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

            @BeforeAll
            static void setUp() {
                printMessage("==[ TEST SELL COMMAND ]==");
            }

            @BeforeEach
            void setUpSellCommand() {
                Shops.getDefaultShop().getItems().clear();
                Shops.getDefaultShop().addItem(emeralds.getType(), 0, -1, 2D, 1.5D);
                Shops.getDefaultShop().addFunds(100);
                mrSparkzz.getInventory().addItem(emeralds);
                // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
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
                double price = Shops.getDefaultShop().getSellPrice(material);

                performCommand(mrSparkzz, "shop sell emerald " + quantity);
                assertEquals(String.format("%sSuccess! You have sold %s%s%s of %s%s%s for %s$%.2f%s.",
                        GREEN, GOLD, quantity, GREEN, GOLD, material, GREEN, GOLD, price * quantity, GREEN), mrSparkzz.nextMessage());
                assertEquals(25, Shops.getDefaultShop().getBalance());
                assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
                printSuccessMessage("sell command test");
            }
        }

        @Nested
        @DisplayName("Transfer Command")
        @Order(9)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class TransferCommandTest {

            @BeforeAll
            static void setUpTransferCommand() {
                printMessage("==[ TEST TRANSFER COMMAND ]==");
            }

            @AfterEach
            void tearDown() {
                Shops.getDefaultShop().setOwner(mrSparkzz.getUniqueId());
            }

            @Test
            @DisplayName("Test Transfer - permissions")
            @Order(1)
            void testTransferCommand_Permissions() {
                performCommand(player2, "shop transfer BetterBuy MrSparkzz");
                assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
                printSuccessMessage("transfer command permission check");
            }

            @Test
            @Disabled("Not sure exactly what's going on with this, but it's having trouble loading either the Server or Player (TransferSubCommand:44)")
            @DisplayName("Test Transfer - main functionality")
            @Order(2)
            void testTransferCommand() {
                performCommand(mrSparkzz, String.format("shop transfer %s %s", Shops.getDefaultShop().getName(), player2.getName()));
                assertEquals(String.format("%sYou have successfully transferred %s%s%s to player %s%s%s!", GREEN, GOLD, Shops.getDefaultShop().getName(), GREEN, GOLD, player2.getName(), GREEN), mrSparkzz.nextMessage());
                assertEquals(player2.getUniqueId(), Shops.getDefaultShop().getOwner());
                printSuccessMessage("transfer command test");
            }
        }

        @Nested
        @DisplayName("Update Command")
        @Order(10)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class UpdateCommandTest {

            private static boolean wasInfStock, wasInfFunds;
            private static String oldName;

            @BeforeAll
            static void saveOldValues() {
                printMessage("==[ TEST UPDATE COMMAND ]==");
                oldName = Shops.getDefaultShop().getName();
                wasInfFunds = Shops.getDefaultShop().hasInfiniteFunds();
                wasInfStock = Shops.getDefaultShop().hasInfiniteStock();
            }

            @AfterEach
            void tearDown() {
                Shops.getDefaultShop().setName(oldName);
                Shops.getDefaultShop().setInfiniteFunds(wasInfFunds);
                Shops.getDefaultShop().setInfiniteStock(wasInfStock);
            }

            @Test
            @DisplayName("Test Update - permissions")
            @Order(1)
            void testUpdateCommand_Permissions() {
                performCommand(player2, "shop update shop-name TestShop99");
                assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
                printSuccessMessage("update command permission check");
            }

            @Test
            @DisplayName("Test Update - main functionality - infinite-funds true")
            @Order(2)
            void testUpdateCommand_InfFunds() {
                performCommand(mrSparkzz, "shop update infinite-funds true");
                assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in the shop!", GREEN, GOLD, "infinite-funds", GREEN, GOLD, "true", GREEN), mrSparkzz.nextMessage());
                assertTrue(Shops.getDefaultShop().hasInfiniteFunds());
                printSuccessMessage("update command test - update infinite funds");
            }

            @Test
            @DisplayName("Test Update - main functionality - inf-stock true")
            @Order(2)
            void testUpdateCommand_InfStock() {
                performCommand(mrSparkzz, "shop update infinite-stock true");
                assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in the shop!", GREEN, GOLD, "infinite-stock", GREEN, GOLD, "true", GREEN), mrSparkzz.nextMessage());
                assertTrue(Shops.getDefaultShop().hasInfiniteStock());
                printSuccessMessage("update command test - update infinite stock");
            }

            @Test
            @DisplayName("Test Update - main functionality - shop name")
            @Order(2)
            void testUpdateCommand_ShopName() {
                performCommand(mrSparkzz, "shop update shop-name TestShop99");
                assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in the shop!", GREEN, GOLD, "shop-name", GREEN, GOLD, "TestShop99", GREEN), mrSparkzz.nextMessage());
                assertEquals("TestShop99", Shops.getDefaultShop().getName());
                printSuccessMessage("update command test - update shop name");
            }
        }

        @Nested
        @DisplayName("Info Command")
        @Order(20)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class InfoCommandTest {

            @BeforeAll
            static void setUp() {
                printMessage("==[ TEST INFO COMMAND ]==");
            }

            @Test
            @DisplayName("Test Info - permissions")
            @Order(1)
            void testUpdateCommand_Permissions() {
                performCommand(player2, "shops");
                assertEquals("§cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is a mistake.", player2.nextMessage());
                printSuccessMessage("info command permission check");
            }

            @Test
            @DisplayName("Test Info")
            @Order(2)
            void testInfoCommand() {
                performCommand(mrSparkzz, "shops");
                assertEquals(String.format("§l§3Shops v%s", plugin.getDescription().getVersion()), mrSparkzz.nextMessage());
                printSuccessMessage("info command test");
            }
        }
    }

    private boolean performCommand(CommandSender sender, String message) {
        return server.dispatchCommand(sender, message);
    }

    private static void printSuccessMessage(String message) {
        plugin.getLogger().info("\u001B[32m[Test] Passed " + message + "\u001B[0m");
    }

    private static void printMessage(String message) {
        plugin.getLogger().info("\u001B[33m" + message + "\u001B[0m");
    }
}