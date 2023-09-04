package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Core;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.*;

import java.util.Collections;

import static net.sparkzz.shops.TestHelper.*;
import static net.sparkzz.shops.util.Notifier.CipherKey.*;
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
        loadConfig();

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        unLoadConfig();
    }

    @BeforeEach
    void setUpWithdrawCommand() {
        Store.setDefaultStore(mrSparkzz.getWorld(), (store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
        Store.getDefaultStore(mrSparkzz.getWorld()).get().setBalance(125);
    }

    @AfterEach
    void tearDownWithdrawCommand() {
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
        player2.setOp(false);
    }

    @Test
    @DisplayName("Test Withdraw - permissions")
    @Order(1)
    void testWithdrawCommand_Permissions() {
        performCommand(player2, "shop withdraw 100");
        assertEquals(Notifier.compose(NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("withdraw command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Withdraw - main functionality - withdraw 100")
    @Order(2)
    void testWithdrawCommand() {
        performCommand(mrSparkzz, "shop withdraw 100");
        assertEquals(Notifier.compose(WITHDRAW_SUCCESS, Collections.singletonMap("amount", 100D)), mrSparkzz.nextMessage());
        assertEquals(25, store.getBalance());
        assertEquals(150, Core.getEconomy().getBalance(mrSparkzz));
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
        assertEquals(Notifier.compose(NOT_OWNER, null), player2.nextMessage());
        assertEquals(125, store.getBalance());
        printSuccessMessage("withdraw command test - not the owner");
    }

    @Test
    @DisplayName("Test Withdraw - main functionality - insufficient funds")
    @Order(5)
    void testWithdrawCommand_InsufficientFunds() {
        performCommand(mrSparkzz, "shop withdraw 200");
        assertEquals(Notifier.compose(INSUFFICIENT_FUNDS_STORE, null), mrSparkzz.nextMessage());
        assertEquals(125, store.getBalance());
        printSuccessMessage("withdraw command test - insufficient funds");
    }

    @Test
    @DisplayName("Test Withdraw - main functionality - no store")
    @Order(6)
    void testWithdrawCommand_NoStore() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop withdraw all");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_STORE_FOUND, null), mrSparkzz.nextMessage());
        printSuccessMessage("withdraw command test - no store");
    }
}
