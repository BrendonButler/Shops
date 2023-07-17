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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Withdraw Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WithdrawCommandTest {

    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST WITHDRAW COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Shops.setDefaultShop(new Store("BetterBuy", mrSparkzz.getUniqueId()));
    }

    @BeforeEach
    void setUpWithdrawCommand() {
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
        Shops.getDefaultShop().addFunds(125);
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Test Withdraw - permissions")
    @Order(1)
    void testWithdrawCommand_Permissions() {
        performCommand(player2, "shop withdraw 100");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("withdraw command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Withdraw - main functionality - withdraw 100")
    @Order(2)
    void testWithdrawCommand() {
        double amount = 100;

        performCommand(mrSparkzz, "shop withdraw " + amount);
        assertEquals(String.format("%sYou have successfully withdrawn %s%s%s from the shop!", GREEN, GOLD, amount, GREEN), mrSparkzz.nextMessage());
        assertEquals(25, Shops.getDefaultShop().getBalance());
        assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("withdraw command test");
    }
}
