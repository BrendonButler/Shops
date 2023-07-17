package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Transfer Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransferCommandTest {

    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUpTransferCommand() {
        printMessage("==[ TEST TRANSFER COMMAND ]==");
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

    @AfterEach
    void resetShop() {
        Shops.getDefaultShop().setOwner(mrSparkzz.getUniqueId());
    }

    @Test
    @DisplayName("Test Transfer - permissions")
    @Order(1)
    void testTransferCommand_Permissions() {
        performCommand(player2, "shop transfer BetterBuy MrSparkzz");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("transfer command permission check");
    }

    @Test
    @Disabled("Not sure exactly what's going on with this, but it's having trouble loading either the Server or Player (TransferSubCommand:44)")
    @DisplayName("Test Transfer - main functionality")
    @Order(2)
    void testTransferCommand() {
        performCommand(mrSparkzz, String.format("shop transfer %s %s", Shops.getDefaultShop().getName(), player2.getName()));
        assertEquals(String.format("%sYou have successfully transferred %s%s%s to player %s%s%s!", GREEN, GOLD, Shops.getDefaultShop().getName(), GREEN, GOLD, player2.getName(), GREEN), mrSparkzz.nextMessage());
        assertEquals(player2.getUniqueId(), Shops.getDefaultShop().getOwner());
        printSuccessMessage("transfer command test");
    }
}
