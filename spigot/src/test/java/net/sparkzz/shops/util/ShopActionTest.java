package net.sparkzz.shops.util;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static net.sparkzz.shops.TestHelper.startServer;
import static net.sparkzz.shops.TestHelper.stopServer;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopActionTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = startServer();
    }

    @AfterEach
    void tearDown() {
        stopServer();
    }

    @Test
    void everyActionNodeIsRegistered() {
        for (ShopAction action : ShopAction.values())
            assertNotNull(server.getPluginManager().getPermission(action.getNode()), action.getNode());
    }

    @Test
    void wildcardGrantsEveryAction() {
        PlayerMock player = server.addPlayer();
        Plugin shops = server.getPluginManager().getPlugin("Shops");

        player.addAttachment(shops, "shops.*", true);

        for (ShopAction action : ShopAction.values())
            assertTrue(Permissions.isPermitted(player, action), action.getNode());
    }
}