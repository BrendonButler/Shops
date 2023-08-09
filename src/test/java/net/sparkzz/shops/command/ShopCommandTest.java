package net.sparkzz.shops.command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock;
import be.seeseemelk.mockbukkit.entity.EntityMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.util.InventoryManagementSystem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Shop Command")
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class ShopCommandTest {

    private static ConsoleCommandSenderMock console;
    private static final ItemStack emeralds = new ItemStack(Material.EMERALD, 64);
    private static PlayerMock mrSparkzz, player;
    private static ServerMock server;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST SHOP COMMAND ]==");
        server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player = server.addPlayer();
        console = new ConsoleCommandSenderMock();

        Shops.setMockServer(server);
        mrSparkzz.getInventory().addItem(emeralds);
        mrSparkzz.setOp(true);
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
        Store.STORES.clear();
    }

    @Order(1)
    @Nested
    @DisplayName("OnTabComplete Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class OnTabCompleteTests {

        @BeforeEach
        void setUpShops() {
            Store.setDefaultStore(new Store("BetterBuy", mrSparkzz.getUniqueId()));
            new Store("DiscountPlus", mrSparkzz.getUniqueId());
        }

        @AfterEach
        void tearDownShops() {
            Store.setDefaultStore(null);
            Store.STORES.clear();
        }

        @Test
        @DisplayName("Test Shop - shop tab complete")
        @Order(1)
        void testShopTabComplete() {
            List<String> expectedOptions = new ArrayList<>(List.of("add", "transfer", "buy", "sell", "create", "deposit", "update", "delete", "remove", "browse", "withdraw"));
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - browse tab complete")
        @Order(20)
        void testShopTabComplete_Browse2Args() {
            List<String> expectedOptions = List.of("<page-number>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop browse ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop browse\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - deposit tab complete")
        @Order(21)
        void testShopTabComplete_Deposit2Args() {
            List<String> expectedOptions = List.of("<amount>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop deposit ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop deposit\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - withdraw tab complete")
        @Order(22)
        void testShopTabComplete_Withdraw2Args() {
            List<String> expectedOptions = List.of("<amount>", "all");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop withdraw ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop withdraw\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - add tab complete")
        @Order(23)
        void testShopTabComplete_Add2Args() {
            List<String> expectedOptions = Arrays.stream(Material.values())
                    .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop add ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop add\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - buy tab complete")
        @Order(24)
        void testShopTabComplete_Buy2Args() {
            Set<Material> shopItems = InventoryManagementSystem.locateCurrentStore(mrSparkzz).getItems().keySet();

            List<String> expectedOptions = Arrays.stream(shopItems.toArray())
                    .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop buy ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop buy\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - remove tab complete")
        @Order(25)
        void testShopTabComplete_Remove2Args() {
            Set<Material> shopItems = InventoryManagementSystem.locateCurrentStore(mrSparkzz).getItems().keySet();

            List<String> expectedOptions = Arrays.stream(shopItems.toArray())
                    .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop remove ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop remove\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - update tab complete when op")
        @Order(26)
        void testShopTabComplete_Update2Args_WhenOp() {
            Set<Material> shopItems = InventoryManagementSystem.locateCurrentStore(mrSparkzz).getItems().keySet();

            List<String> expectedOptions = Arrays.stream(shopItems.toArray())
                    .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());
            expectedOptions.addAll(List.of("infinite-funds", "infinite-stock", "shop-name"));
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update\" when op");
        }

        @Test
        @DisplayName("Test Shop - 2 args - update tab complete when not op")
        @Order(27)
        void testShopTabComplete_Update2Args_WhenNotOp() {
            Set<Material> shopItems = InventoryManagementSystem.locateCurrentStore(player).getItems().keySet();

            List<String> expectedOptions = Arrays.stream(shopItems.toArray())
                    .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());
            expectedOptions.add("shop-name");
            List<String> actualOptions = server.getCommandTabComplete(player, "shop update ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update\" when not op");
        }

        @Test
        @DisplayName("Test Shop - 2 args - sell tab complete")
        @Order(28)
        void testShopTabComplete_Sell2Args() {
            List<String> expectedOptions = Arrays.stream(mrSparkzz.getInventory().getContents())
                    .filter(Objects::nonNull).map(i -> i.getType().toString().toLowerCase())
                    .collect(Collectors.toList());
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop sell ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop sell\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - create tab complete")
        @Order(29)
        void testShopTabComplete_Create2Args() {
            List<String> expectedOptions = List.of("<name>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - delete tab complete")
        @Order(30)
        void testShopTabComplete_Delete2Args() {
            List<String> expectedOptions = Store.STORES.stream().filter(s -> s.getOwner().equals(mrSparkzz.getUniqueId())).map(s -> String.format("%s~%s", s.getName(), s.getUUID())).collect(Collectors.toCollection(ArrayList::new));
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop delete ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop delete\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - transfer tab complete")
        @Order(31)
        void testShopTabComplete_Transfer2Args() {
            List<String> expectedOptions = Store.STORES.stream().filter(s -> s.getOwner().equals(mrSparkzz.getUniqueId())).map(s -> String.format("%s~%s", s.getName(), s.getUUID())).collect(Collectors.toCollection(ArrayList::new));
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop transfer ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop transfer\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - remove tab complete")
        @Order(40)
        void testShopTabComplete_Remove3Args() {
            List<String> expectedOptions = List.of("[<quantity>]", "all");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop remove item ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop remove item\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - sell tab complete")
        @Order(41)
        void testShopTabComplete_Sell3Args() {
            List<String> expectedOptions = List.of("[<quantity>]", "all");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop sell item ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop sell item\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - add tab complete")
        @Order(42)
        void testShopTabComplete_Add3Args() {
            List<String> expectedOptions = List.of("<customer-buy-price>", "[<quantity>]", "all");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop add item ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop add item\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - buy tab complete")
        @Order(43)
        void testShopTabComplete_Buy3Args() {
            List<String> expectedOptions = List.of("[<quantity>]");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop buy item ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop buy item\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - update infinite-funds tab complete")
        @Order(44)
        void testShopTabComplete_Update3Args_InfiniteFunds() {
            List<String> expectedOptions = List.of("true", "false");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update infinite-funds ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update infinite-funds\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - update infinite-stock tab complete")
        @Order(45)
        void testShopTabComplete_Update3Args_InfiniteStock() {
            List<String> expectedOptions = List.of("true", "false");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update infinite-stock ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update infinite-stock\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - update shop-name tab complete")
        @Order(46)
        void testShopTabComplete_Update3Args_ShopName() {
            List<String> expectedOptions = List.of("<name>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update shop-name ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update shop-name\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - update tab complete")
        @Order(47)
        void testShopTabComplete_Update3Args() {
            List<String> expectedOptions = List.of("customer-buy-price", "customer-sell-price", "infinite-quantity", "max-quantity");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update item ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update item\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - transfer tab complete")
        @Order(48)
        void testShopTabComplete_Transfer3Args() {
            List<String> expectedOptions = server.getOnlinePlayers().stream().map(EntityMock::getName).collect(Collectors.toList());
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop transfer shop-name ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop transfer shop-name\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - transfer tab complete")
        @Order(49)
        void testShopTabComplete_Create3Args() {
            List<String> expectedOptions = server.getOnlinePlayers().stream().map(EntityMock::getName).collect(Collectors.toList());
            expectedOptions.add("<x1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create shop-name ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop transfer shop-name\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - add tab complete")
        @Order(60)
        void testShopTabComplete_Add4Args() {
            List<String> expectedOptions = List.of("<customer-sell-price>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop add item 1 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop add item 1\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - update item infinite-quantity tab complete")
        @Order(61)
        void testShopTabComplete_Update4Args_InfiniteQuantity() {
            List<String> expectedOptions = List.of("true", "false");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update item infinite-quantity ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update item infinite-quantity\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - update item infinite-quantity tab complete")
        @Order(62)
        void testShopTabComplete_Update4Args() {
            List<String> expectedOptions = List.of("<value>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update item customer-buy-price ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update item customer-buy-price\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - create tab complete")
        @Order(63)
        void testShopTabComplete_Create4Args() {
            List<String> expectedOptions = List.of("<y1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop 10.5 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop 10.5\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - create tab complete with player")
        @Order(64)
        void testShopTabComplete_Create4Args_Player() {
            List<String> expectedOptions = List.of("<x1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz\"");
        }

        @Test
        @DisplayName("Test Shop - 5 args - add tab complete")
        @Order(80)
        void testShopTabComplete_Add5Args() {
            List<String> expectedOptions = List.of("<max-quantity>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop add item 1 1 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop add item 1 1\"");
        }

        @Test
        @DisplayName("Test Shop - 5 args - create tab complete")
        @Order(81)
        void testShopTabComplete_Create5Args() {
            List<String> expectedOptions = List.of("<z1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop 10.5 64 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop 10.5 64\"");
        }

        @Test
        @DisplayName("Test Shop - 5 args - create tab complete with player")
        @Order(82)
        void testShopTabComplete_Create5Args_Player() {
            List<String> expectedOptions = List.of("<y1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz 10.5 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz 10.5\"");
        }

        @Test
        @DisplayName("Test Shop - 6 args - add tab complete")
        @Order(100)
        void testShopTabComplete_Add6Args() {
            List<String> expectedOptions = List.of("[<quantity>]", "all");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop add item 1 1 1 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop add item 1 1 1\"");
        }

        @Test
        @DisplayName("Test Shop - 6 args - console tab complete")
        @Order(101)
        void testShopTabComplete_Console() {
            List<String> expectedOptions = new ArrayList<>();
            List<String> actualOptions = server.getCommandTabComplete(console, "shop add item 1 1 1 1 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - console");
        }

        @Test
        @DisplayName("Test Shop - 6 args - default tab complete")
        @Order(102)
        void testShopTabComplete_Default() {
            List<String> expectedOptions = new ArrayList<>();
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop add item 1 1 1 1 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - default");
        }

        @Test
        @DisplayName("Test Shop - 6 args - create tab complete")
        @Order(103)
        void testShopTabComplete_Create6Args() {
            List<String> expectedOptions = List.of("<x2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop 10.5 64 -19.2 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop 10.5 64 -19.2\"");
        }

        @Test
        @DisplayName("Test Shop - 6 args - create tab complete with player")
        @Order(104)
        void testShopTabComplete_Create6Args_Player() {
            List<String> expectedOptions = List.of("<z1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz 10.5 64 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz 10.5 64\"");
        }

        @Test
        @DisplayName("Test Shop - 7 args - create tab complete")
        @Order(110)
        void testShopTabComplete_Create7Args() {
            List<String> expectedOptions = List.of("<y2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop 10.5 64 -19.2 60 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop 10.5 64 -19.2\"");
        }

        @Test
        @DisplayName("Test Shop - 7 args - create tab complete with player")
        @Order(111)
        void testShopTabComplete_Create7Args_Player() {
            List<String> expectedOptions = List.of("<x2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz 10.5 64 -19.2 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz 10.5 64 -19.2\"");
        }

        @Test
        @DisplayName("Test Shop - 8 args - create tab complete")
        @Order(120)
        void testShopTabComplete_Create8Args() {
            List<String> expectedOptions = List.of("<z2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop 10.5 64 -19.2 60 20 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop 10.5 64 -19.2 20\"");
        }

        @Test
        @DisplayName("Test Shop - 8 args - create tab complete with player")
        @Order(121)
        void testShopTabComplete_Create8Args_Player() {
            List<String> expectedOptions = List.of("<y2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz 10.5 64 -19.2 60 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz 10.5 64 -19.2 60\"");
        }

        @Test
        @DisplayName("Test Shop - 9 args - create tab complete with player")
        @Order(130)
        void testShopTabComplete_Create9Args_Player() {
            List<String> expectedOptions = List.of("<z2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz 10.5 64 -19.2 60 20 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz 10.5 64 -19.2 60 20\"");
        }
    }

    @Order(2)
    @Nested
    @DisplayName("OnCommand Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class OnCommandTests {

        @Test
        @DisplayName("Test Shop - console sender")
        @Order(1)
        void testShopCommand_ConsoleSender() {
            performCommand(console, "shop");
            assertEquals("§cOnly players can use this command!", console.nextMessage());
            printSuccessMessage("shop command - console sender");
        }

        @Test
        @DisplayName("Test Shop - invalid argument count")
        @Order(2)
        void testShopCommand_InvalidArgumentCount() {
            performCommand(mrSparkzz, "shop");
            assertEquals("§cInvalid number of arguments!", mrSparkzz.nextMessage());
            assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
            printSuccessMessage("shop command - invalid argument count");
        }

        @Test
        @DisplayName("Test Shop - invalid argument count (1 arg)")
        @Order(3)
        void testShopCommand_InvalidArgumentCountOneArg() {
            performCommand(mrSparkzz, "shop test");
            assertEquals("§cInvalid number of arguments!", mrSparkzz.nextMessage());
            assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
            printSuccessMessage("shop command - invalid argument count (1 arg)");
        }

        @Test
        @DisplayName("Test Shop - invalid subcommand")
        @Order(4)
        void testShopCommand_InvalidSubcommand() {
            performCommand(mrSparkzz, "shop test 1");
            assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
            printSuccessMessage("shop command - invalid subcommand");
        }
    }
}
