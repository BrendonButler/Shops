package net.sparkzz.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.util.Config;
import net.sparkzz.util.Cuboid;
import net.sparkzz.util.Notifier;
import org.bukkit.Location;
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

import java.util.Collections;
import java.util.Map;

import static net.sparkzz.shops.TestHelper.*;
import static net.sparkzz.util.Notifier.CipherKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Create Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CreateCommandTest {

    private static double previousMaxVolume, previousMinVolume;
    private static double[] previousMaxDimensions;
    private static PlayerMock mrSparkzz, player2;
    private static World world;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST CREATE COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);
        loadConfig();
        world = server.createWorld(WorldCreator.name("world"));

        Shops.setMockServer(server);
        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        mrSparkzz.setLocation(new Location(world, 0D, 0D, 0D));
        previousMaxDimensions = Config.getMaxDimensions();
        previousMinVolume = Config.getMinVolume();
        previousMaxVolume = Config.getMaxVolume();
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        unLoadConfig();
        Store.DEFAULT_STORES.clear();
    }

    @AfterEach
    void tearDownStore() {
        Store.STORES.clear();
        Config.setMaxDimensions(previousMaxDimensions[0], previousMaxDimensions[1], previousMaxDimensions[2]);
        Config.setMinVolume(previousMinVolume);
        Config.setMaxVolume(previousMaxVolume);
    }

    @BeforeEach
    void setUpStore() {
        Store.setDefaultStore(mrSparkzz.getWorld(), new Store("BetterBuy", mrSparkzz.getUniqueId(), new Cuboid(world, -50, 10, -25, -100, 60, -50)));
    }

    @Test
    @DisplayName("Test Create - permissions")
    @Order(1)
    void testCreateShop_Permissions() {
        performCommand(player2, "shop create TestShop");
        assertEquals(Notifier.compose(NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("create command permission check");
    }

    @Test
    @DisplayName("Test Create - main functionality")
    @Order(2)
    void testCreateShop() {
        performCommand(mrSparkzz, "shop create TestShop");
        assertEquals(Notifier.compose(STORE_CREATE_SUCCESS, Collections.singletonMap("store", "TestShop")), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - creation of TestShop");
    }

    @Test
    @DisplayName("Test Create - main functionality - another player as owner")
    @Order(3)
    void testCreateShop_ForAnotherPlayer() {
        performCommand(mrSparkzz, String.format("shop create TestShop %s", player2.getName()));
        assertEquals(Notifier.compose(STORE_CREATE_SUCCESS_OTHER_PLAYER, Map.of("store", "TestShop", "target", player2.getName())), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - creation of TestShop for Player0");
    }

    @Test
    @DisplayName("Test Create - main functionality - another player (by UUID) as owner")
    @Order(4)
    void testCreateShop_ForAnotherPlayerByUUID() {
        // TODO: create MockPermissions to add specific permissions to a player mock
        performCommand(mrSparkzz, String.format("shop create TestShop %s", player2.getUniqueId()));
        assertEquals(Notifier.compose(STORE_CREATE_SUCCESS_OTHER_PLAYER, Map.of("store", "TestShop", "target", player2.getUniqueId())), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - creation of TestShop for Player0 by UUID");
    }

    @Test
    @DisplayName("Test Create - main functionality - target player not found")
    @Order(5)
    void testCreateCommand_NoTargetPlayer() {
        performCommand(mrSparkzz, "shop create BetterBuy Player99");
        assertEquals(Notifier.compose(PLAYER_NOT_FOUND, Collections.singletonMap("target", "Player99")), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - target player not found");
    }

    @Test
    @DisplayName("Test Create - main functionality - cuboid shop")
    @Order(6)
    void testCreateCommand_CuboidShop() {
        performCommand(mrSparkzz, "shop create BetterBuy 10.5 8 -23 15 0 -37");
        assertEquals(Notifier.compose(STORE_CREATE_SUCCESS, Collections.singletonMap("store", "BetterBuy")), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - cuboid shop");
    }

    @Test
    @DisplayName("Test Create - main functionality - cuboid shop for other player")
    @Order(7)
    void testCreateCommand_CuboidShop_OtherPlayer() {
        performCommand(mrSparkzz, String.format("shop create TestShop %s 10.5 8 -23 15 0 -37", player2.getUniqueId()));
        assertEquals(Notifier.compose(STORE_CREATE_SUCCESS_OTHER_PLAYER, Map.of("store", "TestShop", "target", player2.getUniqueId())), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - cuboid shop for other player");
    }

    @Test
    @DisplayName("Test Create - main functionality - max stores")
    @Order(8)
    void testCreateCommand_MaxStores() {
        new Store("store2", mrSparkzz.getUniqueId());
        new Store("store3", mrSparkzz.getUniqueId());
        new Store("store4", mrSparkzz.getUniqueId());
        new Store("store5", mrSparkzz.getUniqueId());
        performCommand(mrSparkzz, "shop create TestShop");
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_MAX_STORES, Collections.singletonMap("max-stores", Config.getMaxOwnedStores())), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - max stores");
    }

    @Test
    @DisplayName("Test Create - main functionality - off-limits area")
    @Order(9)
    void testCreateCommand_CuboidShop_OffLimitsArea() {
        performCommand(mrSparkzz, "shop create TestShop 4 0 4 16 5 16");
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_OFFLIMITS, null), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - off-limits area");
    }

    @Test
    @DisplayName("Test Create - main functionality - overlaps other store")
    @Order(10)
    void testCreateCommand_CuboidShop_OverlapsExisting() {
        new Store("GuardShop", player2.getUniqueId(), new Cuboid(world, 200, 200, 200, 220, 220, 220));
        performCommand(mrSparkzz, "shop create TestShop 205 205 205 208 208 208");
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_OVERLAPS, null), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - off-limits area");
    }

    @Test
    @DisplayName("Test Create - limits - under minimum X")
    @Order(20)
    void testCreateCommand_Limits_UnderMinimumX() {
        performCommand(mrSparkzz, "shop create TestShop 10 12 16 11 20 40");
        double[] minimum = Config.getMinDimensions();
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_MIN_DIMS, Map.of(
                "limit-min-x", minimum[0],
                "limit-min-y", minimum[1],
                "limit-min-z", minimum[2]
        )), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - under minimum x");
    }

    @Test
    @DisplayName("Test Create - limits - under minimum Y")
    @Order(21)
    void testCreateCommand_Limits_UnderMinimumY() {
        performCommand(mrSparkzz, "shop create TestShop 10 12 16 20 13 40");
        double[] minimum = Config.getMinDimensions();
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_MIN_DIMS, Map.of(
                "limit-min-x", minimum[0],
                "limit-min-y", minimum[1],
                "limit-min-z", minimum[2]
        )), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - under minimum y");
    }

    @Test
    @DisplayName("Test Create - limits - under minimum Z")
    @Order(22)
    void testCreateCommand_Limits_UnderMinimumZ() {
        performCommand(mrSparkzz, "shop create TestShop 10 12 16 20 20 17");
        double[] minimum = Config.getMinDimensions();
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_MIN_DIMS, Map.of(
                "limit-min-x", minimum[0],
                "limit-min-y", minimum[1],
                "limit-min-z", minimum[2]
        )), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - under minimum z");
    }

    @Test
    @DisplayName("Test Create - limits - over maximum X")
    @Order(23)
    void testCreateCommand_Limits_OverMinimumX() {
        performCommand(mrSparkzz, "shop create TestShop 10 12 16 100 20 40");
        double[] maximum = Config.getMaxDimensions();
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_MAX_DIMS, Map.of(
                "limit-max-x", maximum[0],
                "limit-max-y", maximum[1],
                "limit-max-z", maximum[2]
        )), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - over maximum x");
    }

    @Test
    @DisplayName("Test Create - limits - over maximum Y")
    @Order(24)
    void testCreateCommand_Limits_OverMinimumY() {
        performCommand(mrSparkzz, "shop create TestShop 10 12 16 20 100 40");
        double[] maximum = Config.getMaxDimensions();
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_MAX_DIMS, Map.of(
                "limit-max-x", maximum[0],
                "limit-max-y", maximum[1],
                "limit-max-z", maximum[2]
        )), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - over maximum y");
    }

    @Test
    @DisplayName("Test Create - limits - over maximum Z")
    @Order(25)
    void testCreateCommand_Limits_OverMinimumZ() {
        performCommand(mrSparkzz, "shop create TestShop 10 12 16 20 20 100");
        double[] maximum = Config.getMaxDimensions();
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_MAX_DIMS, Map.of(
                "limit-max-x", maximum[0],
                "limit-max-y", maximum[1],
                "limit-max-z", maximum[2]
        )), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - over maximum z");
    }

    @Test
    @DisplayName("Test Create - limits - zero maximum X")
    @Order(26)
    void testCreateCommand_Limits_ZeroMaximumX() {
        Config.setMaxDimensions(0D, 0D ,0D);
        Config.setMaxVolume(27000D);
        performCommand(mrSparkzz, "shop create TestShop 120 120 120 150 150 150");
        assertEquals(Notifier.compose(STORE_CREATE_SUCCESS, Collections.singletonMap("store", "TestShop")), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - zero maximum x");
    }

    @Test
    @DisplayName("Test Create - limits - zero maximum Y")
    @Order(26)
    void testCreateCommand_Limits_ZeroMaximumY() {
        Config.setMaxDimensions(35D, 0D ,0D);
        Config.setMaxVolume(27000D);
        performCommand(mrSparkzz, "shop create TestShop 120 120 120 150 150 150");
        assertEquals(Notifier.compose(STORE_CREATE_SUCCESS, Collections.singletonMap("store", "TestShop")), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - zero maximum y");
    }

    @Test
    @DisplayName("Test Create - limits - zero maximum Z")
    @Order(26)
    void testCreateCommand_Limits_ZeroMaximumZ() {
        Config.setMaxDimensions(35D, 35D ,0D);
        Config.setMaxVolume(27000D);
        performCommand(mrSparkzz, "shop create TestShop 120 120 120 150 150 150");
        assertEquals(Notifier.compose(STORE_CREATE_SUCCESS, Collections.singletonMap("store", "TestShop")), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - zero maximum z");
    }

    @Test
    @DisplayName("Test Create - limits - minimum volume")
    @Order(27)
    void testCreateCommand_Limits_MinimumVolume() {
        Config.setMinVolume(28D);
        performCommand(mrSparkzz, "shop create TestShop 120 120 120 123 123 123");
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_MIN_VOL, Collections.singletonMap("limit-min-vol", Config.getMinVolume())), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - minimum volume");
    }

    @Test
    @DisplayName("Test Create - limits - maximum volume")
    @Order(28)
    void testCreateCommand_Limits_MaximumVolume() {
        Config.setMaxVolume(20D);
        performCommand(mrSparkzz, "shop create TestShop 120 120 120 123 123 123");
        assertEquals(Notifier.compose(STORE_CREATE_FAIL_MAX_VOL, Collections.singletonMap("limit-max-vol", Config.getMaxVolume())), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - limits - maximum volume");
    }

    @Test
    @DisplayName("Test Create - main functionality - zero max volume")
    @Order(29)
    void testCreateCommand_Limits_ZeroMaxVolume() {
        Config.setMaxDimensions(0D, 0D, 0D);
        Config.setMaxVolume(0);
        performCommand(mrSparkzz, "shop create TestShop 205 205 205 308 308 308");
        assertEquals(Notifier.compose(STORE_CREATE_SUCCESS, Collections.singletonMap("store", "TestShop")), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - zero max volume");
    }
}
