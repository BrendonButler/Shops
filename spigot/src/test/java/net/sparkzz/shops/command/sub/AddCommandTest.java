package net.sparkzz.shops.command.sub;

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
import java.util.Objects;

import static net.sparkzz.shops.TestHelper.*;
import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Add Command")
class AddCommandTest {

    private static ItemStack emeralds;
    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUpAddCommand() {
        printMessage("==[ TEST ADD COMMAND ]==");
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
    void setUpEach() {
        Store store = new Store("BetterBuy", mrSparkzz.getUniqueId());
        store.addItem(emeralds.getType(), 10, -1, 2D, 1.5D);
        store.addFunds(BigDecimal.valueOf(100));
        Store.setDefaultStore(mrSparkzz.getWorld(), store);
        mrSparkzz.getInventory().addItem(emeralds);
    }

    @AfterEach
    void tearDownEach() {
        Store.STORES.clear();
        mrSparkzz.getInventory().clear();
    }

    @Test
    @DisplayName("Test Add - permissions")
    void testAddCommand_Permissions() {
        performCommand(player2, "shop add acacia_log 1");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("add command permission check");
    }

    @Test
    @DisplayName("Test Add - main functionality - add 1")
    void testAddCommand_AddOne() {
        Material material = emeralds.getType();

        performCommand(mrSparkzz, "shop add emerald 1");
        assertEquals(Notifier.compose(ADD_SUCCESS_QUANTITY, Map.of("quantity", 1, "material", material)), mrSparkzz.nextMessage());
        assertEquals(63, Objects.requireNonNull(mrSparkzz.getInventory().getItem(0)).getAmount());
        assertEquals(11, Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(material).get("quantity").intValue());
        printSuccessMessage("add command test - add 1");
    }

    @Test
    @DisplayName("Test Add - main functionality - add all")
    void testAddCommand_AddAll() {
        Material material = emeralds.getType();
        int quantity = emeralds.getAmount();

        performCommand(mrSparkzz, "shop add emerald all");
        assertEquals(Notifier.compose(ADD_SUCCESS_QUANTITY, Map.of("quantity", quantity, "material", material)), mrSparkzz.nextMessage());
        assertFalse(mrSparkzz.getInventory().contains(material));
        // 10 starting stock plus the 64 added here
        assertEquals(74, Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(material).get("quantity").intValue());
        printSuccessMessage("add command test - add all");
    }

    @Test
    @DisplayName("Test Add - material not found in shop")
    void testAddCommand_NoMaterial() {
        mrSparkzz.getInventory().addItem(new ItemStack(Material.STICK, 64));
        performCommand(mrSparkzz, "shop add stick 1");
        assertEquals(Notifier.compose(MATERIAL_MISSING_STORE, Collections.singletonMap("material", Material.STICK)), mrSparkzz.nextMessage());
        printSuccessMessage("add command - material doesn't exist");
    }

    @Test
    @DisplayName("Test Add - invalid material")
    void testRemoveCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop add emeral 1");
        assertEquals(Notifier.compose(INVALID_MATERIAL, Collections.singletonMap("material", "emeral")), mrSparkzz.nextMessage());
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - invalid material");
    }

    @Test
    @DisplayName("Test Add - main functionality - no store")
    void testDepositCommand_NoStore() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop add emerald 1");
        assertEquals(Notifier.compose(NO_STORE_FOUND, null), mrSparkzz.nextMessage());
        printSuccessMessage("add command test - no store");
    }
}
