package net.sparkzz.shops.command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Info Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InfoCommandTest {

    private static PlayerMock mrSparkzz, player2;
    private static Shops plugin;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST INFO COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        plugin = MockBukkit.load(Shops.class);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Shops.setDefaultShop(new Store("BetterBuy", mrSparkzz.getUniqueId()));
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Test Info - permissions")
    @Order(1)
    void testInfoCommand_Permissions() {
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
