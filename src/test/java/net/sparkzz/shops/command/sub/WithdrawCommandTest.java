package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.*;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Withdraw Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WithdrawCommandTest {

    private static PlayerMock mrSparkzz, player2;
    private static Store store;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST WITHDRAW COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Shops.setDefaultShop((store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
        Store.STORES.clear();
    }

    @BeforeEach
    void setUpWithdrawCommand() {
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
        Shops.getDefaultShop().setBalance(125);
    }

    @AfterEach
    void tearDownWithdrawCommand() {
        player2.setOp(false);
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
        assertEquals(25, store.getBalance());
        assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("withdraw command test");
    }

    @Test
    @DisplayName("Test Withdraw - main functionality - invalid amount")
    @Order(3)
    void testWithdrawCommand_InvalidAmount() {
        performCommand(mrSparkzz, "shop withdraw fail");
        assertEquals("§cInvalid numerical value (fail)!", mrSparkzz.nextMessage());
        assertEquals("/shop withdraw <amount>", mrSparkzz.nextMessage());
        assertEquals(125, store.getBalance());
        printSuccessMessage("withdraw command test - invalid amount");
    }

    @Test
    @DisplayName("Test Withdraw - main functionality - not the owner")
    @Order(4)
    void testWithdrawCommand_NotOwner() {
        player2.setOp(true);
        performCommand(player2, "shop withdraw 100");
        assertEquals("§cYou are not the owner of this store, you cannot perform this command!", player2.nextMessage());
        assertEquals(125, store.getBalance());
        printSuccessMessage("withdraw command test - not the owner");
    }

    @Test
    @DisplayName("Test Withdraw - main functionality - insufficient funds")
    @Order(5)
    void testWithdrawCommand_InsufficientFunds() {
        performCommand(mrSparkzz, "shop withdraw 200");
        assertEquals("§cThe store has insufficient funds!", mrSparkzz.nextMessage());
        assertEquals(125, store.getBalance());
        printSuccessMessage("withdraw command test - insufficient funds");
    }
}
