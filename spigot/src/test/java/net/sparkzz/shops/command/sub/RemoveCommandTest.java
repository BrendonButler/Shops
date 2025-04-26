package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static net.sparkzz.shops.TestHelper.*;
import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Remove Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RemoveCommandTest {

    private static final ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUpRemoveCommand() {
        printMessage("==[ TEST REMOVE COMMAND ]==");
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
    void setUp() {
        Store.setDefaultStore(mrSparkzz.getWorld(), new Store("BetterBuy", mrSparkzz.getUniqueId()));
        Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().clear();
        Store.getDefaultStore(mrSparkzz.getWorld()).get().addItem(emeralds.getType(), emeralds.getAmount(), -1, 2D, 1.5D);
    }

    @AfterEach
    void tearDownEach() {
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
    }

    @Test
    @DisplayName("Test Remove - permissions")
    @Order(1)
    void testRemoveCommand_Permissions() {
        performCommand(player2, "shop remove acacia_log 1");
        assertEquals(Notifier.compose(NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("remove command permission check");
    }

    @Test
    @DisplayName("Test Remove - main functionality - remove 1")
    @Order(2)
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
    @Order(3)
    void testRemoveCommand_RemoveAll() {
        Material material = emeralds.getType();

        performCommand(mrSparkzz, "shop remove emerald");
        assertEquals(Notifier.compose(REMOVE_SUCCESS, Collections.singletonMap("material", Material.EMERALD)), mrSparkzz.nextMessage());
        assertNull(Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(material));
        printSuccessMessage("remove command test - remove all of type from shop");
    }

    @Test
    @DisplayName("Test Remove - material not found in store")
    @Order(4)
    void testRemoveCommand_NoMaterial() {
        performCommand(mrSparkzz, "shop remove stick 1");
        assertEquals(Notifier.compose(MATERIAL_MISSING_STORE, Collections.singletonMap("material", Material.STICK)), mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - material doesn't exist");
    }

    @Test
    @DisplayName("Test Remove - invalid material")
    @Order(5)
    void testRemoveCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop remove emeral 1");
        assertEquals(Notifier.compose(INVALID_MATERIAL, Collections.singletonMap("material", "emeral")), mrSparkzz.nextMessage());
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - invalid material");
    }

    @Test
    @DisplayName("Test Remove - main functionality - no store")
    @Order(6)
    void testRemoveCommand_NoStore() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop remove emerald 1");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_STORE_FOUND, null), mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - no store");
    }

    @Test
    @DisplayName("Test Remove - main functionality - insufficient inventory store")
    @Order(7)
    void testRemoveCommand_InsufficientInventoryStore() {
        performCommand(mrSparkzz, "shop remove emerald 1000");
        assertEquals(Notifier.compose(INSUFFICIENT_INV_STORE, Collections.singletonMap("material", Material.EMERALD)), mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - insufficient inventory store");
    }

    @Test
    @DisplayName("Test Remove - main functionality - insufficient inventory player")
    @Order(8)
    void testRemoveCommand_InsufficientInventoryPlayer() {
        mrSparkzz.getInventory().addItem(new ItemStack(Material.MILK_BUCKET, 2304));
        performCommand(mrSparkzz, "shop remove emerald 64");
        assertEquals(Notifier.compose(REMOVE_INSUFFICIENT_INV_PLAYER, Collections.singletonMap("material", Material.EMERALD)), mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - insufficient inventory player");
    }
}
