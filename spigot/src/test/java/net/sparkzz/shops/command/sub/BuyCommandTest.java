package net.sparkzz.shops.command.sub;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static net.sparkzz.shops.TestHelper.*;
import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Buy Command")
class BuyCommandTest {

    private static ItemStack emeralds;

    private static PlayerMock mrSparkzz, player2;
    private static Store store;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST BUY COMMAND ]==");
        ServerMock server = startServer();
        emeralds = new ItemStack(Material.EMERALD, 64);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
    }

    @AfterAll
    static void tearDown() {
        stopServer();
    }

    @BeforeEach
    void setUpBuyCommand() {
        Store.setDefaultStore(mrSparkzz.getWorld(), (store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
        store.addItem(emeralds.getType(), emeralds.getAmount(), -1, 2D, 1.5D);
        store.setBalance(BigDecimal.ZERO);
        setBalance(mrSparkzz, 50);
    }

    @AfterEach
    void tearDownEach() {
        mrSparkzz.getInventory().clear();
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
    }

    @Test
    @DisplayName("Test Buy - permissions")
    void testBuyCommand_Permissions() {
        withoutPermission(player2, "shops.cmd.buy", () -> performCommand(player2, "shop buy emerald 1"));
        assertEquals(Notifier.compose(NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("buy command permission check");
    }

    @Test
    @DisplayName("Test Buy - main functionality")
    void testBuyCommand() {
        performCommand(mrSparkzz, "shop buy emerald 12");
        assertEquals(Notifier.compose(BUY_SUCCESS, Map.of("quantity", 12, "material", Material.EMERALD, "cost", 24D)), mrSparkzz.nextMessage());
        assertEquals(24D, store.getBalance().doubleValue());
        assertEquals(26, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test");
    }

    @Test
    @DisplayName("Test Buy - main functionality - below minimum amount")
    void testBuyCommand_BelowMinimum() {
        performCommand(mrSparkzz, "shop buy emerald -1");
        assertEquals(Notifier.compose(INVALID_QUANTITY, Collections.singletonMap("quantity", -1)), mrSparkzz.nextMessage());
        assertEquals(BigDecimal.ZERO, store.getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - below minimum amount");
    }

    @Test
    @DisplayName("Test Buy - main functionality - above maximum amount")
    void testBuyCommand_AboveMaximum() {
        performCommand(mrSparkzz, "shop buy emerald 2305");
        assertEquals(Notifier.compose(INVALID_QUANTITY, Collections.singletonMap("quantity", 2305)), mrSparkzz.nextMessage());
        assertEquals(BigDecimal.ZERO, store.getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - above maximum amount");
    }

    @Test
    @DisplayName("Test Buy - main functionality - invalid material")
    void testBuyCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop buy emeral 10");
        assertEquals(Notifier.compose(INVALID_MATERIAL, Collections.singletonMap("material", "emeral")), mrSparkzz.nextMessage());
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        assertEquals(BigDecimal.ZERO, store.getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - above maximum amount");
    }

    @Test
    @DisplayName("Test Buy - main functionality - query price")
    void testBuyCommand_QueryPrice() {
        performCommand(mrSparkzz, "shop buy emerald");
        assertEquals(Notifier.compose(PRICE, Collections.singletonMap("cost", 2D)), mrSparkzz.nextMessage());
        assertEquals(BigDecimal.ZERO, store.getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - query price");
    }

    @Test
    @DisplayName("Test Buy - main functionality - no store")
    void testBuyCommand_NoStore() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop buy emerald 2");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_STORE_FOUND, null), mrSparkzz.nextMessage());
        assertEquals(BigDecimal.ZERO, store.getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test - no store");
    }
}