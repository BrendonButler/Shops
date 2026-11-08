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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static net.sparkzz.shops.TestHelper.*;
import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Remove Command")
public class RemoveCommandTest {

    private static ItemStack emeralds;

    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUpRemoveCommand() {
        printMessage("==[ TEST REMOVE COMMAND ]==");
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
    void setUp() {
        Store.setDefaultStore(mrSparkzz.getWorld(), new Store("BetterBuy", mrSparkzz.getUniqueId()));
        Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().clear();
        Store.getDefaultStore(mrSparkzz.getWorld()).get().addItem(emeralds.getType(), emeralds.getAmount(), -1, 2D, 1.5D);
    }

    @AfterEach
    void tearDownEach() {
        mrSparkzz.getInventory().clear();
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
    }

    @Test
    @DisplayName("Test Remove - permissions")
    void testRemoveCommand_Permissions() {
        performCommand(player2, "shop remove acacia_log 1");
        assertEquals(Notifier.compose(NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("remove command permission check");
    }

    @Test
    @DisplayName("Test Remove - main functionality - remove 1")
    void testRemoveCommand_RemoveOne() {
        Material material = emeralds.getType();

        performCommand(mrSparkzz, "shop remove emerald 1");
        assertEquals(Notifier.compose(REMOVE_SUCCESS_QUANTITY, Map.of("material", Material.EMERALD, "quantity", 1)), mrSparkzz.nextMessage());
        assertEquals(1, Objects.requireNonNull(mrSparkzz.getInventory().getItem(0)).getAmount());
        assertEquals(63, Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(material).get("quantity").intValue());
        printSuccessMessage("remove command test - remove 1 of type from shop");
    }

    @Test
    @DisplayName("Test Remove - main functionality - remove all")
    void testRemoveCommand_RemoveAll() {
        Material material = emeralds.getType();

        performCommand(mrSparkzz, "shop remove emerald");
        assertEquals(Notifier.compose(REMOVE_SUCCESS, Collections.singletonMap("material", Material.EMERALD)), mrSparkzz.nextMessage());
        assertNull(Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(material));
        printSuccessMessage("remove command test - remove all of type from shop");
    }

    @Test
    @DisplayName("Test Remove - material not found in store")
    void testRemoveCommand_NoMaterial() {
        performCommand(mrSparkzz, "shop remove stick 1");
        assertEquals(Notifier.compose(MATERIAL_MISSING_STORE, Collections.singletonMap("material", Material.STICK)), mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - material doesn't exist");
    }

    @Test
    @DisplayName("Test Remove - invalid material")
    void testRemoveCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop remove emeral 1");
        assertEquals(Notifier.compose(INVALID_MATERIAL, Collections.singletonMap("material", "emeral")), mrSparkzz.nextMessage());
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - invalid material");
    }

    @Test
    @DisplayName("Test Remove - main functionality - no store")
    void testRemoveCommand_NoStore() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop remove emerald 1");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_STORE_FOUND, null), mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - no store");
    }

    @Test
    @DisplayName("Test Remove - main functionality - insufficient inventory store")
    void testRemoveCommand_InsufficientInventoryStore() {
        performCommand(mrSparkzz, "shop remove emerald 1000");
        assertEquals(Notifier.compose(INSUFFICIENT_INV_STORE, Collections.singletonMap("material", Material.EMERALD)), mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - insufficient inventory store");
    }

    @Test
    @DisplayName("Test Remove - main functionality - insufficient inventory player")
    void testRemoveCommand_InsufficientInventoryPlayer() {
        mrSparkzz.getInventory().addItem(new ItemStack(Material.MILK_BUCKET, 2304));
        performCommand(mrSparkzz, "shop remove emerald 64");
        assertEquals(Notifier.compose(REMOVE_INSUFFICIENT_INV_PLAYER, Collections.singletonMap("material", Material.EMERALD)), mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - insufficient inventory player");
    }
}
