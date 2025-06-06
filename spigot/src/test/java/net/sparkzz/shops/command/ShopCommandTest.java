package net.sparkzz.shops.command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock;
import be.seeseemelk.mockbukkit.entity.EntityMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Collections;
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
        loadConfig();

        mrSparkzz = server.addPlayer("MrSparkzz");
        player = server.addPlayer();
        console = new ConsoleCommandSenderMock();

        Shops.setServerInstance(server);
        mrSparkzz.getInventory().addItem(emeralds);
        mrSparkzz.setOp(true);
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        unLoadConfig();
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Order(1)
    @Nested
    @DisplayName("OnTabComplete Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class OnTabCompleteTests {

        @BeforeEach
        void setUpShops() {
            Store.setDefaultStore(mrSparkzz.getWorld(), new Store("BetterBuy", mrSparkzz.getUniqueId()));
            new Store("DiscountPlus", mrSparkzz.getUniqueId());
            new Store("DiscountMinus", mrSparkzz.getUniqueId());
        }

        @AfterEach
        void tearDownShops() {
            Store.DEFAULT_STORES.clear();
            Store.STORES.clear();
        }

        @Test
        @DisplayName("Test Shop - shop tab complete")
        @Order(1)
        void testShopTabComplete() {
            List<String> expectedOptions = List.of("add", "transfer", "buy", "sell", "create", "deposit", "update", "delete", "remove", "browse", "withdraw");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - browse tab complete")
        @Order(20)
        void testShopTabComplete_Browse2Args() {
            List<String> expectedOptions = Collections.singletonList("<page-number>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop browse ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop browse\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - deposit tab complete")
        @Order(21)
        void testShopTabComplete_Deposit2Args() {
            List<String> expectedOptions = Collections.singletonList("<amount>");
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
            mrSparkzz.getInventory().addItem(new ItemStack(Material.EMERALD, 64));
            mrSparkzz.getInventory().addItem(new ItemStack(Material.STICK, 32));

            List<String> expectedOptions = Arrays.stream(mrSparkzz.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .map(s -> s.getType().toString().toLowerCase()).toList();
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop add ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop add\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - buy tab complete")
        @Order(24)
        void testShopTabComplete_Buy2Args() {
            Set<Material> shopItems = InventoryManagementSystem.locateCurrentStore(mrSparkzz).get().getItems().keySet();

            List<String> expectedOptions = Arrays.stream(shopItems.toArray())
                    .map(m -> m.toString().toLowerCase()).toList();
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop buy ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop buy\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - remove tab complete")
        @Order(25)
        void testShopTabComplete_Remove2Args() {
            Set<Material> shopItems = InventoryManagementSystem.locateCurrentStore(mrSparkzz).get().getItems().keySet();

            List<String> expectedOptions = Arrays.stream(shopItems.toArray())
                    .map(m -> m.toString().toLowerCase()).toList();
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop remove ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop remove\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - update tab complete when op")
        @Order(26)
        void testShopTabComplete_Update2Args_WhenOp() {
            Set<Material> shopItems = InventoryManagementSystem.locateCurrentStore(mrSparkzz).get().getItems().keySet();

            List<String> expectedOptions = Arrays.stream(shopItems.toArray())
                    .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());
            expectedOptions.addAll(List.of("infinite-funds", "infinite-stock", "location", "store-name"));
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update\" when op");
        }

        @Test
        @DisplayName("Test Shop - 2 args - update tab complete when not op")
        @Order(27)
        void testShopTabComplete_Update2Args_WhenNotOp() {
            Set<Material> shopItems = InventoryManagementSystem.locateCurrentStore(player).get().getItems().keySet();

            List<String> expectedOptions = Arrays.stream(shopItems.toArray())
                    .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());
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
                    .toList();
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop sell ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop sell\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - create tab complete")
        @Order(29)
        void testShopTabComplete_Create2Args() {
            List<String> expectedOptions = Collections.singletonList("<name>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - delete tab complete")
        @Order(30)
        void testShopTabComplete_Delete2Args() {
            List<String> expectedOptions = Store.STORES.stream().filter(s -> s.getOwner().equals(mrSparkzz.getUniqueId())).map(s -> String.format("%s~%s", s.getName(), s.getUUID())).toList();
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop delete ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop delete\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - transfer tab complete")
        @Order(31)
        void testShopTabComplete_Transfer2Args() {
            List<String> expectedOptions = Store.STORES.stream().filter(s -> s.getOwner().equals(mrSparkzz.getUniqueId())).map(s -> String.format("%s~%s", s.getName(), s.getUUID())).toList();
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
            List<String> expectedOptions = Collections.singletonList("[<quantity>]");
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
            List<String> expectedOptions = Collections.singletonList("<name>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update store-name ");

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
        @DisplayName("Test Shop - 3 args - update tab complete - location")
        @Order(48)
        void testShopTabComplete_Update3Args_Location() {
            List<String> expectedOptions = Store.STORES.stream().filter(s -> s.getOwner().equals(mrSparkzz.getUniqueId())).map(s -> String.format("%s~%s", s.getName(), s.getUUID())).collect(Collectors.toList());
            expectedOptions.addAll(Bukkit.getWorlds().stream().map(WorldInfo::getName).toList());
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - transfer tab complete")
        @Order(49)
        void testShopTabComplete_Transfer3Args() {
            List<String> expectedOptions = server.getOnlinePlayers().stream().map(EntityMock::getName).toList();
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop transfer shop-name ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop transfer shop-name\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - transfer tab complete")
        @Order(50)
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
            List<String> expectedOptions = Collections.singletonList("<customer-sell-price>");
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
            List<String> expectedOptions = Collections.singletonList("<value>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update item customer-buy-price ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update item customer-buy-price\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - update location DiscountMinus tab complete")
        @Order(63)
        void testShopTabComplete_Update4Args_LocationWithStore() {
            List<String> expectedOptions = Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList());
            expectedOptions.add("<x1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location DiscountMinus ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location DiscountMinus\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - update location 10 tab complete")
        @Order(64)
        void testShopTabComplete_Update4Args_LocationWithoutStore() {
            List<String> expectedOptions = Collections.singletonList("<y1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location 10 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location 10\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - update location world tab complete")
        @Order(65)
        void testShopTabComplete_Update4Args_LocationWithoutStoreWithWorld() {
            List<String> expectedOptions = Collections.singletonList("<x1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location world ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location world\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - create tab complete")
        @Order(66)
        void testShopTabComplete_Create4Args() {
            List<String> expectedOptions = Collections.singletonList("<y1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop 10.5 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop 10.5\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - create tab complete with player")
        @Order(67)
        void testShopTabComplete_Create4Args_Player() {
            List<String> expectedOptions = Collections.singletonList("<x1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz\"");
        }

        @Test
        @DisplayName("Test Shop - 5 args - add tab complete")
        @Order(80)
        void testShopTabComplete_Add5Args() {
            List<String> expectedOptions = Collections.singletonList("<max-quantity>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop add item 1 1 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop add item 1 1\"");
        }

        @Test
        @DisplayName("Test Shop - 5 args - create tab complete")
        @Order(81)
        void testShopTabComplete_Create5Args() {
            List<String> expectedOptions = Collections.singletonList("<z1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop 10.5 64 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop 10.5 64\"");
        }

        @Test
        @DisplayName("Test Shop - 5 args - create tab complete with player")
        @Order(82)
        void testShopTabComplete_Create5Args_Player() {
            List<String> expectedOptions = Collections.singletonList("<y1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz 10.5 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz 10.5\"");
        }

        @Test
        @DisplayName("Test Shop - 5 args - update location DiscountMinus world tab complete")
        @Order(83)
        void testShopTabComplete_Update5Args_LocationWithStore() {
            List<String> expectedOptions = Collections.singletonList("<x1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location DiscountMinus world ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location DiscountMinus world\"");
        }

        @Test
        @DisplayName("Test Shop - 5 args - update location 10 20 tab complete")
        @Order(84)
        void testShopTabComplete_Update5Args_LocationWithoutStore() {
            List<String> expectedOptions = Collections.singletonList("<z1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location 10 20 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location 10 20\"");
        }

        @Test
        @DisplayName("Test Shop - 5 args - update location world 10 tab complete")
        @Order(85)
        void testShopTabComplete_Update5Args_LocationWithoutStoreWithWorld() {
            List<String> expectedOptions = Collections.singletonList("<y1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location world 10 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location world 10\"");
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
            List<String> expectedOptions = Collections.emptyList();
            List<String> actualOptions = server.getCommandTabComplete(console, "shop add item 1 1 1 1 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - console");
        }

        @Test
        @DisplayName("Test Shop - 6 args - default tab complete")
        @Order(102)
        void testShopTabComplete_Default() {
            List<String> expectedOptions = Collections.emptyList();
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop add item 1 1 1 1 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - default");
        }

        @Test
        @DisplayName("Test Shop - 6 args - create tab complete")
        @Order(103)
        void testShopTabComplete_Create6Args() {
            List<String> expectedOptions = Collections.singletonList("<x2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop 10.5 64 -19.2 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop 10.5 64 -19.2\"");
        }

        @Test
        @DisplayName("Test Shop - 6 args - create tab complete with player")
        @Order(104)
        void testShopTabComplete_Create6Args_Player() {
            List<String> expectedOptions = Collections.singletonList("<z1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz 10.5 64 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz 10.5 64\"");
        }

        @Test
        @DisplayName("Test Shop - 6 args - update location DiscountMinus world 10 tab complete")
        @Order(105)
        void testShopTabComplete_Update6Args_LocationWithStore() {
            List<String> expectedOptions = Collections.singletonList("<y1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location DiscountMinus world 10 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location DiscountMinus world 10 \"");
        }

        @Test
        @DisplayName("Test Shop - 6 args - update location 10 20 30 tab complete")
        @Order(106)
        void testShopTabComplete_Update6Args_LocationWithoutStore() {
            List<String> expectedOptions = Collections.singletonList("<x2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location 10 20 30 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location 10 20 30\"");
        }

        @Test
        @DisplayName("Test Shop - 6 args - update location world 10 20 tab complete")
        @Order(107)
        void testShopTabComplete_Update6Args_LocationWithoutStoreWithWorld() {
            List<String> expectedOptions = Collections.singletonList("<z1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location world 10 20 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location world 10 20\"");
        }

        @Test
        @DisplayName("Test Shop - 7 args - create tab complete")
        @Order(110)
        void testShopTabComplete_Create7Args() {
            List<String> expectedOptions = Collections.singletonList("<y2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop 10.5 64 -19.2 60 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop 10.5 64 -19.2\"");
        }

        @Test
        @DisplayName("Test Shop - 7 args - create tab complete with player")
        @Order(111)
        void testShopTabComplete_Create7Args_Player() {
            List<String> expectedOptions = Collections.singletonList("<x2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz 10.5 64 -19.2 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz 10.5 64 -19.2\"");
        }

        @Test
        @DisplayName("Test Shop - 7 args - update location DiscountMinus world 10 20 tab complete")
        @Order(112)
        void testShopTabComplete_Update7Args_LocationWithStore() {
            List<String> expectedOptions = Collections.singletonList("<z1>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location DiscountMinus world 10 20 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location DiscountMinus world 10 20\"");
        }

        @Test
        @DisplayName("Test Shop - 7 args - update location 10 20 30 40 tab complete")
        @Order(113)
        void testShopTabComplete_Update7Args_LocationWithoutStore() {
            List<String> expectedOptions = Collections.singletonList("<y2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location 10 20 30 40 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location 10 20 30 40\"");
        }

        @Test
        @DisplayName("Test Shop - 7 args - update location world 10 20 30 tab complete")
        @Order(114)
        void testShopTabComplete_Update7Args_LocationWithoutStoreWithWorld() {
            List<String> expectedOptions = Collections.singletonList("<x2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location world 10 20 30 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location world 10 20 30\"");
        }

        @Test
        @DisplayName("Test Shop - 8 args - create tab complete")
        @Order(120)
        void testShopTabComplete_Create8Args() {
            List<String> expectedOptions = Collections.singletonList("<z2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop 10.5 64 -19.2 60 20 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop 10.5 64 -19.2 20\"");
        }

        @Test
        @DisplayName("Test Shop - 8 args - create tab complete with player")
        @Order(121)
        void testShopTabComplete_Create8Args_Player() {
            List<String> expectedOptions = Collections.singletonList("<y2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz 10.5 64 -19.2 60 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz 10.5 64 -19.2 60\"");
        }

        @Test
        @DisplayName("Test Shop - 8 args - update location DiscountMinus world 10 20 30 tab complete")
        @Order(122)
        void testShopTabComplete_Update8Args_LocationWithStore() {
            List<String> expectedOptions = Collections.singletonList("<x2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location DiscountMinus world 10 20 30 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location DiscountMinus world 10 20 30\"");
        }

        @Test
        @DisplayName("Test Shop - 8 args - update location 10 20 30 40 50 tab complete")
        @Order(123)
        void testShopTabComplete_Update8Args_LocationWithoutStore() {
            List<String> expectedOptions = Collections.singletonList("<z2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location 10 20 30 40 50 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location 10 20 30 40 50\"");
        }

        @Test
        @DisplayName("Test Shop - 8 args - update location world 10 20 30 40 tab complete")
        @Order(124)
        void testShopTabComplete_Update8Args_LocationWithoutStoreWithWorld() {
            List<String> expectedOptions = Collections.singletonList("<y2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location world 10 20 30 40 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location world 10 20 30 40\"");
        }

        @Test
        @DisplayName("Test Shop - 9 args - create tab complete with player")
        @Order(130)
        void testShopTabComplete_Create9Args_Player() {
            List<String> expectedOptions = Collections.singletonList("<z2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop create TestShop MrSparkzz 10.5 64 -19.2 60 20 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop create TestShop MrSparkzz 10.5 64 -19.2 60 20\"");
        }

        @Test
        @DisplayName("Test Shop - 9 args - update location DiscountMinus world 10 20 30 40 tab complete")
        @Order(131)
        void testShopTabComplete_Update9Args_LocationWithStore() {
            List<String> expectedOptions = Collections.singletonList("<y2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location DiscountMinus world 10 20 30 40 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location DiscountMinus world 10 20 30 40\"");
        }

        @Test
        @DisplayName("Test Shop - 9 args - update location world 10 20 30 40 50 tab complete")
        @Order(132)
        void testShopTabComplete_Update9Args_LocationWithoutStoreWithWorld() {
            List<String> expectedOptions = Collections.singletonList("<z2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location world 10 20 30 40 50 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location world 10 20 30 40 50\"");
        }

        @Test
        @DisplayName("Test Shop - 10 args - update location DiscountMinus world 10 20 30 40 50 tab complete")
        @Order(140)
        void testShopTabComplete_Update10Args_LocationWithStore() {
            List<String> expectedOptions = Collections.singletonList("<z2>");
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop update location DiscountMinus world 10 20 30 40 50 ");

            assertEquals(expectedOptions, actualOptions);
            printSuccessMessage("tab complete - \"shop update location DiscountMinus world 10 20 30 40 50\"");
        }

        @Test
        @DisplayName("Test Shop - 2 args - invalid option")
        @Order(151)
        void testShopTabComplete_2Args_InvalidOption() {
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop invalid ");

            assertEquals(Collections.emptyList(), actualOptions);
            printSuccessMessage("tab complete - \"shop invalid\"");
        }

        @Test
        @DisplayName("Test Shop - 3 args - invalid option")
        @Order(152)
        void testShopTabComplete_3Args_InvalidOption() {
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop invalid 2 ");

            assertEquals(Collections.emptyList(), actualOptions);
            printSuccessMessage("tab complete - \"shop invalid 2\"");
        }

        @Test
        @DisplayName("Test Shop - 4 args - invalid option")
        @Order(153)
        void testShopTabComplete_4Args_InvalidOption() {
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop invalid 2 3 ");

            assertEquals(Collections.emptyList(), actualOptions);
            printSuccessMessage("tab complete - \"shop invalid 2 3\"");
        }

        @Test
        @DisplayName("Test Shop - 5 args - invalid option")
        @Order(154)
        void testShopTabComplete_5Args_InvalidOption() {
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop invalid 2 3 4 ");

            assertEquals(Collections.emptyList(), actualOptions);
            printSuccessMessage("tab complete - \"shop invalid 2 3 4\"");
        }

        @Test
        @DisplayName("Test Shop - 6 args - invalid option")
        @Order(155)
        void testShopTabComplete_6Args_InvalidOption() {
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop invalid 2 3 4 5 ");

            assertEquals(Collections.emptyList(), actualOptions);
            printSuccessMessage("tab complete - \"shop invalid 2 3 4 5\"");
        }

        @Test
        @DisplayName("Test Shop - 7 args - invalid option")
        @Order(156)
        void testShopTabComplete_7Args_InvalidOption() {
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop invalid 2 3 4 5 6 ");

            assertEquals(Collections.emptyList(), actualOptions);
            printSuccessMessage("tab complete - \"shop invalid 2 3 4 5 6\"");
        }

        @Test
        @DisplayName("Test Shop - 8 args - invalid option")
        @Order(157)
        void testShopTabComplete_8Args_InvalidOption() {
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop invalid 2 3 4 5 6 7 ");

            assertEquals(Collections.emptyList(), actualOptions);
            printSuccessMessage("tab complete - \"shop invalid 2 3 4 5 6 7\"");
        }

        @Test
        @DisplayName("Test Shop - 9 args - invalid option")
        @Order(158)
        void testShopTabComplete_9Args_InvalidOption() {
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop invalid 2 3 4 5 6 7 8 ");

            assertEquals(Collections.emptyList(), actualOptions);
            printSuccessMessage("tab complete - \"shop invalid 2 3 4 5 6 7 8\"");
        }

        @Test
        @DisplayName("Test Shop - 10 args - invalid option")
        @Order(159)
        void testShopTabComplete_10Args_InvalidOption() {
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop invalid 2 3 4 5 6 7 8 9 ");

            assertEquals(Collections.emptyList(), actualOptions);
            printSuccessMessage("tab complete - \"shop invalid 2 3 4 5 6 7 8 9\"");
        }

        @Test
        @DisplayName("Test Shop - 11 args - invalid option")
        @Order(160)
        void testShopTabComplete_11Args_InvalidOption() {
            List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shop invalid 2 3 4 5 6 7 8 9 10 ");

            assertEquals(Collections.emptyList(), actualOptions);
            printSuccessMessage("tab complete - \"shop invalid 2 3 4 5 6 7 8 9 10\"");
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
            assertEquals(Notifier.compose(Notifier.CipherKey.ONLY_PLAYERS_CMD, null), console.nextMessage());
            printSuccessMessage("shop command - console sender");
        }

        @Test
        @DisplayName("Test Shop - invalid argument count")
        @Order(2)
        void testShopCommand_InvalidArgumentCount() {
            performCommand(mrSparkzz, "shop");
            assertEquals(Notifier.compose(Notifier.CipherKey.INVALID_ARG_CNT, null), mrSparkzz.nextMessage());
            assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
            printSuccessMessage("shop command - invalid argument count");
        }

        @Test
        @DisplayName("Test Shop - invalid argument count (1 arg)")
        @Order(3)
        void testShopCommand_InvalidArgumentCountOneArg() {
            performCommand(mrSparkzz, "shop test");
            assertEquals(Notifier.compose(Notifier.CipherKey.INVALID_ARG_CNT, null), mrSparkzz.nextMessage());
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
