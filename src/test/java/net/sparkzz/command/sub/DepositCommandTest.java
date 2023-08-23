package net.sparkzz.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.util.Notifier;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.*;

import java.util.Collections;

import static net.sparkzz.shops.TestHelper.*;
import static net.sparkzz.util.Notifier.CipherKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
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
    void setUpDepositCommand() {
        Store.setDefaultStore(mrSparkzz.getWorld(), (store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 150);
        Store.getDefaultStore(mrSparkzz.getWorld()).get().setBalance(25);
    }

    @AfterEach
    void tearDownWithdrawCommand() {
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
        player2.setOp(false);
    }

    @Test
    @DisplayName("Test Deposit - permissions")
    @Order(1)
    void testWithdrawCommand_Permissions() {
        performCommand(player2, "shop deposit 100");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("deposit command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Deposit - main functionality - deposit 100")
    @Order(2)
    void testDepositCommand() {
        performCommand(mrSparkzz, "shop deposit 100");
        assertEquals(Notifier.compose(DEPOSIT_SUCCESS, Collections.singletonMap("amount", 100D)), mrSparkzz.nextMessage());
        assertEquals(125, Store.getDefaultStore(mrSparkzz.getWorld()).get().getBalance());
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
        assertEquals(Notifier.compose(NOT_OWNER, null), player2.nextMessage());
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
        assertEquals(Notifier.compose(INSUFFICIENT_FUNDS_PLAYER, null), mrSparkzz.nextMessage());
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
        assertEquals(Notifier.compose(DEPOSIT_INF_FUNDS, null), mrSparkzz.nextMessage());
        // TODO: assertEquals(125, Shops.getEconomy().getBalance(mrSparkzz));
        assertEquals(25, store.getBalance());
        printSuccessMessage("deposit command test - shop has infinite funds");
    }

    @Test
    @DisplayName("Test Deposit - main functionality - no store")
    @Order(7)
    void testDepositCommand_NoStore() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop deposit 100");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_STORE_FOUND, null), mrSparkzz.nextMessage());
        printSuccessMessage("deposit command test - no store");
    }
}
