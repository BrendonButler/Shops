package net.sparkzz.shops.command;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.BuildInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Collections;
import java.util.List;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Info Command")
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
    void testInfoCommand() {
        performCommand(mrSparkzz, "shops");
        // MockBukkit doesn't load the plugin from a JAR, so there's no manifest and the placeholders are expected
        assertEquals(String.format("§3Shops v%s (%s, build %s)", plugin.getDescription().getVersion(),
                BuildInfo.UNKNOWN_COMMIT, BuildInfo.UNKNOWN_BUILD), mrSparkzz.nextMessage());
        printSuccessMessage("info command test");
    }

    @Test
    @DisplayName("Test Info - info tab complete")
    void testShopTabComplete() {
        List<String> actualOptions = server.getCommandTabComplete(mrSparkzz, "shops ");

        assertEquals(Collections.emptyList(), actualOptions);
        printSuccessMessage("tab complete - \"shops\"");
    }
}
