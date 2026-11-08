package net.sparkzz.shops.command;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Collections;
import java.util.List;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Info Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InfoCommandTest {

    private static PlayerMock mrSparkzz, player2;
    private static ServerMock server;
    private static Shops plugin;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST INFO COMMAND ]==");
        server = startServer();

        plugin = (Shops) server.getPluginManager().getPlugin("Shops");

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Store.setDefaultStore(mrSparkzz.getWorld(), new Store("BetterBuy", mrSparkzz.getUniqueId()));
    }

    @AfterAll
    static void tearDown() {
        stopServer();
    }

    @Test
    @DisplayName("Test Info - permissions")
    @Order(1)
    void testInfoCommand_Permissions() {
        withoutPermission(player2, "shops.cmd.shops", () -> performCommand(player2, "shops"));
        // The server sends its own no-permission message; only check that the command was blocked
        String message = player2.nextMessage();
        assertNotNull(message);
        assertTrue(message.contains("do not have permission"), message);
        printSuccessMessage("info command permission check");
    }

    @Test
    @DisplayName("Test Info")
    @Order(2)
    void testInfoCommand() {
        performCommand(mrSparkzz, "shops");
        assertEquals(String.format("§3Shops v%s (local, build dev)", plugin.getDescription().getVersion()), mrSparkzz.nextMessage());
        printSuccessMessage("info command test");
    }

    @Test
    @DisplayName("Test Info - info tab complete")
    @Order(3)
    void testShopTabComplete() {
        List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shops ");

        assertEquals(Collections.emptyList(), actualOptions);
        printSuccessMessage("tab complete - \"shops\"");
    }
}
