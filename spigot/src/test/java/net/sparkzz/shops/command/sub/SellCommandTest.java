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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static net.sparkzz.shops.TestHelper.*;
import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Sell Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SellCommandTest {
    private static ItemStack emeralds;

    private static PlayerMock mrSparkzz, player2;
    private static Store store;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST SELL COMMAND ]==");
        ServerMock server = startServer();
        emeralds = new ItemStack(Material.EMERALD, 64);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Store.setDefaultStore(mrSparkzz.getWorld(), (store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
    }

    @AfterAll
    static void tearDown() {
        stopServer();
    }

    @BeforeEach
    void setUpSellCommand() {
        Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().clear();
        Store.getDefaultStore(mrSparkzz.getWorld()).get().addItem(emeralds.getType(), 0, -1, 2D, 1.5D);
        Store.getDefaultStore(mrSparkzz.getWorld()).get().setBalance(BigDecimal.valueOf(100));
        mrSparkzz.getInventory().addItem(emeralds);
        setBalance(mrSparkzz, 50);
    }

    @AfterEach
    void tearDownEach() {
        mrSparkzz.getInventory().clear();
    }

    @Test
    @DisplayName("Test Sell - permissions")
    @Order(1)
    void testSellCommand_Permissions() {
        withoutPermission(player2, "shops.cmd.sell", () -> performCommand(player2, "shop sell emerald 1"));
        assertEquals(Notifier.compose(NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("sell command permission check");
    }

    @Test
    @DisplayName("Test Sell - main functionality - sell 1")
    @Order(2)
    void testSellCommand() {
        performCommand(mrSparkzz, "shop sell emerald 1");
        assertEquals(Notifier.compose(SELL_SUCCESS, Map.of("quantity", 1, "material", Material.EMERALD, "cost", 1.5D)), mrSparkzz.nextMessage());
        assertEquals(98.5D, Store.getDefaultStore(mrSparkzz.getWorld()).get().getBalance().doubleValue());
        assertEquals(51.5D, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test");
    }

    @Test
    @DisplayName("Test Sell - main functionality - below minimum amount")
    @Order(3)
    void testSellCommand_BelowMinimum() {
        performCommand(mrSparkzz, "shop sell emerald -1");
        assertEquals(Notifier.compose(INVALID_QUANTITY, Collections.singletonMap("quantity", -1)), mrSparkzz.nextMessage());
        assertEquals(BigDecimal.valueOf(100), store.getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - below minimum amount");
    }

    @Test
    @DisplayName("Test Sell - main functionality - above maximum amount")
    @Order(4)
    void testSellCommand_AboveMaximum() {
        performCommand(mrSparkzz, "shop sell emerald 2305");
        assertEquals(Notifier.compose(INVALID_QUANTITY, Collections.singletonMap("quantity", 2305)), mrSparkzz.nextMessage());
        assertEquals(BigDecimal.valueOf(100), store.getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - above maximum amount");
    }

    @Test
    @DisplayName("Test Sell - main functionality - invalid material")
    @Order(5)
    void testSellCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop sell emeral 1");
        assertEquals(Notifier.compose(INVALID_MATERIAL, Collections.singletonMap("material", "emeral")), mrSparkzz.nextMessage());
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        assertEquals(BigDecimal.valueOf(100), store.getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - above maximum amount");
    }

    @Test
    @DisplayName("Test Sell - main functionality - query price")
    @Order(6)
    void testSellCommand_QueryPrice() {
        performCommand(mrSparkzz, "shop sell emerald");
        assertEquals(Notifier.compose(PRICE, Collections.singletonMap("cost", 1.5D)), mrSparkzz.nextMessage());
        assertEquals(BigDecimal.valueOf(100), store.getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - query price");
    }

    @Test
    @DisplayName("Test Sell - main functionality - no store")
    @Order(7)
    void testSellCommand_NoStore() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop sell emerald 2");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_STORE_FOUND, null), mrSparkzz.nextMessage());
        assertEquals(BigDecimal.valueOf(100), store.getBalance());
        assertEquals(50, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test - no store");
    }
}
