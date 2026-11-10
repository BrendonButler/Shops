package net.sparkzz.shops.util;

import net.sparkzz.shops.Store;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Optional;
import java.util.UUID;

import static net.sparkzz.shops.TestHelper.startServer;
import static net.sparkzz.shops.TestHelper.stopServer;
import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.NOT_OWNER;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Spigot permissions")
class PermissionsTest {

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

    private PlayerMock playerWithAdd() {
        PlayerMock player = server.addPlayer();

        player.addAttachment(shops, ShopAction.ADD.getNode(), true);

        return player;
    }

    @Test
    @DisplayName("A player is recognized as their store's owner")
    void owner_isPermitted() {
        PlayerMock player = playerWithAdd();
        Store store = new Store("TestStore", player.getUniqueId());

        assertTrue(Permissions.isPermitted(player, ShopAction.ADD, store));
    }

    @Test
    @DisplayName("A non-owner is denied with the not-owner message")
    void nonOwner_isDenied() {
        PlayerMock player = playerWithAdd();
        Store store = new Store("TestStore", UUID.randomUUID());

        assertFalse(Permissions.isPermitted(player, ShopAction.ADD, store));
        assertEquals(Optional.of(NOT_OWNER), Permissions.denialFor(player, ShopAction.ADD, store));
    }

    @Test
    @DisplayName("The console has no identity but is permitted through the override")
    void console_isPermittedThroughOverride() {
        Store store = new Store("TestStore", UUID.randomUUID());

        assertEquals(Optional.empty(), Permissions.denialFor(server.getConsoleSender(), ShopAction.ADD, store));
    }
}