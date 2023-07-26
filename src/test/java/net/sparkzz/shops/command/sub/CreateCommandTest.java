package net.sparkzz.shops.command.sub;

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
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Create Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CreateCommandTest {

    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST CREATE COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

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
        // TODO: create MockPermissions to add specific permissions to a player mock
        performCommand(mrSparkzz, "shop create TestShop");
        assertEquals(String.format("%sYou have successfully created %s%s%s!", GREEN, GOLD, "TestShop", GREEN), mrSparkzz.nextMessage());
        printSuccessMessage("create command test - creation of TestShop");
    }
}
