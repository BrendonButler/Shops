package net.sparkzz.shops.command;

import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Permission gates")
class PermissionGateTest {

    private ServerMock server;
    private Plugin shops;

    @BeforeEach
    void setUp() {
        server = startServer();
        shops = server.getPluginManager().getPlugin("Shops");
    }

    @AfterEach
    void tearDown() {
        stopServer();
    }

    private PlayerMock playerWithOnlyUpdate() {
        PlayerMock player = server.addPlayer();

        player.addAttachment(shops, "shops.cmd.update", true);
        Store.setDefaultStore(player.getWorld(), new Store("TestStore", player.getUniqueId()));

        return player;
    }

    @Test
    @DisplayName("Non-op can run /shops")
    void nonOp_canRunInfo() {
        PlayerMock player = server.addPlayer();

        performCommand(player, "shops");

        assertTrue(player.nextMessage().contains("Shops v"));
    }

    @Test
    @DisplayName("Op with a negated node is denied")
    void op_withNegatedNode_isDenied() {
        PlayerMock player = server.addPlayer();

        player.setOp(true);
        player.addAttachment(shops, "shops.cmd.browse", false);
        Store.setDefaultStore(player.getWorld(), new Store("TestStore", player.getUniqueId()));

        performCommand(player, "shop browse");

        assertEquals(Notifier.compose(Notifier.CipherKey.NO_PERMS_CMD, null), player.nextMessage());
    }

    @Test
    @DisplayName("Update suggests only the options the player can use")
    void updateOptions_hideOptionsWithoutPermission() {
        PlayerMock player = playerWithOnlyUpdate();

        assertEquals(List.of("store-name"), server.getCommandTabComplete(player, "shop update "));
    }

    @Test
    @DisplayName("Permitted update option still completes")
    void updateStoreName_stillCompletes() {
        PlayerMock player = playerWithOnlyUpdate();

        assertEquals(List.of("<name>"), server.getCommandTabComplete(player, "shop update store-name "));
    }

    @Test
    @DisplayName("Update location is hidden without its permission")
    void updateLocation_withoutPermission_isEmpty() {
        PlayerMock player = playerWithOnlyUpdate();

        assertEquals(List.of(), server.getCommandTabComplete(player, "shop update location "));
    }

    @Test
    @DisplayName("Update infinite-funds is hidden without its permission")
    void updateInfiniteFunds_withoutPermission_isEmpty() {
        PlayerMock player = playerWithOnlyUpdate();

        assertEquals(List.of(), server.getCommandTabComplete(player, "shop update infinite-funds "));
    }

    @Test
    @DisplayName("Item fields still complete with only the update permission")
    void updateItem_stillCompletes() {
        PlayerMock player = playerWithOnlyUpdate();

        assertFalse(server.getCommandTabComplete(player, "shop update diamond ").isEmpty());
    }

    @Test
    @DisplayName("Five or more update arguments need the location permission")
    void updateFiveArgs_withoutLocation_isEmpty() {
        PlayerMock player = playerWithOnlyUpdate();

        assertEquals(List.of(), server.getCommandTabComplete(player, "shop update diamond 1 2 "));
    }

    @Test
    @DisplayName("Create is hidden without its permission")
    void create_withoutPermission_isEmpty() {
        PlayerMock player = playerWithOnlyUpdate();

        assertEquals(List.of(), server.getCommandTabComplete(player, "shop create x "));
    }
}