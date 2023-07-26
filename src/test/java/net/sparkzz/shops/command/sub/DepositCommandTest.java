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

@DisplayName("Deposit Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DepositCommandTest {

    private static PlayerMock mrSparkzz, player2;
    private static Store store;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST DEPOSIT COMMAND ]==");
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
    }

    @BeforeEach
    void setUpDepositCommand() {
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 150);
        Shops.getDefaultShop().setBalance(25);
    }

    @AfterEach
    void tearDownWithdrawCommand() {
        player2.setOp(false);
        store.setInfiniteFunds(false);
    }

    @Test
    @DisplayName("Test Deposit - permissions")
    @Order(1)
    void testWithdrawCommand_Permissions() {
        performCommand(player2, "shop deposit 100");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("deposit command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Deposit - main functionality - deposit 100")
    @Order(2)
    void testDepositCommand() {
        double amount = 100;

        performCommand(mrSparkzz, "shop deposit " + amount);
        assertEquals(String.format("%sYou have successfully deposited %s%s%s to the shop!", GREEN, GOLD, amount, GREEN), mrSparkzz.nextMessage());
        assertEquals(125, Shops.getDefaultShop().getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("deposit command test");
    }

    @Test
    @DisplayName("Test Deposit - main functionality - invalid amount")
    @Order(3)
    void testDepositCommand_InvalidAmount() {
        performCommand(mrSparkzz, "shop deposit fail");
        assertEquals("§cInvalid numerical value (fail)!", mrSparkzz.nextMessage());
        assertEquals("/shop deposit <amount>", mrSparkzz.nextMessage());
        // TODO: assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
        assertEquals(25, store.getBalance());
        printSuccessMessage("deposit command test - invalid amount");
    }

    @Test
    @DisplayName("Test Deposit - main functionality - not the owner")
    @Order(4)
    void testDepositCommand_NotOwner() {
        player2.setOp(true);
        performCommand(player2, "shop deposit 100");
        assertEquals("§cYou are not the owner of this store, you cannot perform this command!", player2.nextMessage());
        // TODO: assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
        assertEquals(25, store.getBalance());
        printSuccessMessage("deposit command test - not the owner");
    }

    @Test
    @Disabled
    @DisplayName("Test Deposit - main functionality - insufficient funds")
    @Order(5)
    void testDepositCommand_InsufficientFunds() {
        performCommand(mrSparkzz, "shop deposit 200");
        assertEquals("§cYou have insufficient funds!", mrSparkzz.nextMessage());
        assertEquals(125, Shops.getEconomy().getBalance(mrSparkzz));
        assertEquals(25, store.getBalance());
        printSuccessMessage("deposit command test - insufficient funds");
    }

    @Test
    @DisplayName("Test Deposit - main functionality - shop has infinite funds")
    @Order(6)
    void testDepositCommand_InfiniteFunds() {
        store.setInfiniteFunds(true);
        performCommand(mrSparkzz, "shop deposit 15");
        assertEquals("§aThis store has infinite funds, depositing funds isn't necessary!", mrSparkzz.nextMessage());
        // TODO: assertEquals(125, Shops.getEconomy().getBalance(mrSparkzz));
        assertEquals(25, store.getBalance());
        printSuccessMessage("deposit command test - shop has infinite funds");
    }
}
