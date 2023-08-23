package net.sparkzz.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.util.Cuboid;
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

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Create Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CreateCommandTest {

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
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("create command permission check");
    }

    @Test
    @DisplayName("Test Create - main functionality")
    @Order(2)
    void testCreateShop() {
        performCommand(mrSparkzz, "shop create TestShop");
        assertEquals(String.format("%sYou have successfully created %s%s%s!", GREEN, GOLD, "TestShop", GREEN), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - creation of TestShop");
    }

    @Test
    @DisplayName("Test Create - main functionality - another player as owner")
    @Order(3)
    void testCreateShop_ForAnotherPlayer() {
        performCommand(mrSparkzz, String.format("shop create TestShop %s", player2.getName()));
        assertEquals(String.format("§aYou have successfully created §6TestShop§a for §6%s§a!", player2.getName()), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - creation of TestShop for Player0");
    }

    @Test
    @DisplayName("Test Create - main functionality - another player (by UUID) as owner")
    @Order(4)
    void testCreateShop_ForAnotherPlayerByUUID() {
        // TODO: create MockPermissions to add specific permissions to a player mock
        performCommand(mrSparkzz, String.format("shop create TestShop %s", player2.getUniqueId()));
        assertEquals(String.format("§aYou have successfully created §6TestShop§a for §6%s§a!", player2.getUniqueId()), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - creation of TestShop for Player0 by UUID");
    }

    @Test
    @DisplayName("Test Create - main functionality - target player not found")
    @Order(5)
    void testCreateCommand_NoTargetPlayer() {
        performCommand(mrSparkzz, "shop create BetterBuy Player99");
        assertEquals("§cPlayer (Player99) not found!", mrSparkzz.nextMessage());
        printSuccessMessage("create command test - target player not found");
    }

    @Test
    @DisplayName("Test Create - main functionality - cuboid shop")
    @Order(6)
    void testCreateCommand_CuboidShop() {
        performCommand(mrSparkzz, "shop create BetterBuy 10.5 8 -23 15 0 -37");
        assertEquals("§aYou have successfully created §6BetterBuy§a!", mrSparkzz.nextMessage());
        printSuccessMessage("create command test - cuboid shop");
    }

    @Test
    @DisplayName("Test Create - main functionality - cuboid shop for other player")
    @Order(7)
    void testCreateCommand_CuboidShop_OtherPlayer() {
        performCommand(mrSparkzz, String.format("shop create TestShop %s 10.5 8 -23 15 0 -37", player2.getUniqueId()));
        assertEquals(String.format("§aYou have successfully created §6TestShop§a for §6%s§a!", player2.getUniqueId()), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - cuboid shop for other player");
    }
}
