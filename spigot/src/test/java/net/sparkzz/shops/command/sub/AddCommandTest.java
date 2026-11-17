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
import java.util.List;
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
        Store.DEFAULT_STORES.clear();
        mrSparkzz.getInventory().clear();
        player2.getInventory().clear();
    }

    private static int defaultStoreQuantity(Material material) {
        return Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(material).get("quantity").intValue();
    }

    /**
     * Replaces the default store with one owned by player2, so player2 can run add as a non-op owner
     *
     * @param withEmeralds whether the store should already list emeralds
     */
    private static void useStoreOwnedByPlayer2(boolean withEmeralds) {
        Store store = new Store("PlayerStore", player2.getUniqueId());

        if (withEmeralds) store.addItem(Material.EMERALD, 0, -1, 2D, 1.5D);

        Store.setDefaultStore(player2.getWorld(), store);
        player2.getInventory().addItem(emeralds.clone());
    }

    @Test
    @DisplayName("Test Add - not the owner")
    void testAddCommand_NotOwner() {
        player2.getInventory().addItem(emeralds.clone());

        withPermission(player2, "shops.cmd.add", () -> performCommand(player2, "shop add emerald 1"));
        assertEquals(Notifier.compose(NOT_OWNER, null), player2.nextMessage());
        assertEquals(64, Objects.requireNonNull(player2.getInventory().getItem(0)).getAmount());
        assertEquals(10, defaultStoreQuantity(Material.EMERALD));
        printSuccessMessage("add command test - not the owner");
    }

    @Test
    @DisplayName("Test Add - not the owner - admin override")
    void testAddCommand_NotOwner_Override() {
        player2.getInventory().addItem(emeralds.clone());

        withPermissions(player2, List.of("shops.cmd.add", "shops.admin.override"), () -> performCommand(player2, "shop add emerald 1"));
        assertEquals(Notifier.compose(ADD_SUCCESS_QUANTITY, Map.of("quantity", 1, "material", Material.EMERALD)), player2.nextMessage());
        assertEquals(63, Objects.requireNonNull(player2.getInventory().getItem(0)).getAmount());
        assertEquals(11, defaultStoreQuantity(Material.EMERALD));
        printSuccessMessage("add command test - not the owner with admin override");
    }

    @Test
    @DisplayName("Test Add - non-op owner - add 1")
    void testAddCommand_NonOpOwner_AddOne() {
        useStoreOwnedByPlayer2(true);

        withPermission(player2, "shops.cmd.add", () -> performCommand(player2, "shop add emerald 1"));
        assertEquals(Notifier.compose(ADD_SUCCESS_QUANTITY, Map.of("quantity", 1, "material", Material.EMERALD)), player2.nextMessage());
        assertEquals(1, defaultStoreQuantity(Material.EMERALD));
        printSuccessMessage("add command test - non-op owner adds 1");
    }

    @Test
    @DisplayName("Test Add - non-op owner - add new material")
    void testAddCommand_NonOpOwner_AddNewMaterial() {
        useStoreOwnedByPlayer2(false);

        withPermission(player2, "shops.cmd.add", () -> performCommand(player2, "shop add emerald 2 1.5 64 1"));
        assertEquals(Notifier.compose(ADDED_MATERIAL_TO_STORE_QUANTITY, Map.of(
                "quantity", 1, "material", Material.EMERALD, "buy-price", 2D, "sell-price", 1.5D, "max-quantity", 64)), player2.nextMessage());
        assertEquals(1, defaultStoreQuantity(Material.EMERALD));
        printSuccessMessage("add command test - non-op owner adds a new material");
    }

    @Test
    @DisplayName("Test Add - main functionality - insufficient player stock")
    void testAddCommand_InsufficientPlayerStock() {
        performCommand(mrSparkzz, "shop add emerald 100");
        assertEquals(Notifier.compose(INSUFFICIENT_STOCK_PLAYER, null), mrSparkzz.nextMessage());
        assertEquals(64, Objects.requireNonNull(mrSparkzz.getInventory().getItem(0)).getAmount());
        assertEquals(10, defaultStoreQuantity(Material.EMERALD));
        printSuccessMessage("add command test - insufficient player stock");
    }

    @Test
    @DisplayName("Test Add - infinite stock - new material")
    void testAddCommand_InfiniteStock_NewMaterial() {
        performCommand(mrSparkzz, "shop add diamond 2 1.5 64 -1");
        assertEquals(Notifier.compose(ADDED_MATERIAL_TO_STORE, Map.of(
                "material", Material.DIAMOND, "buy-price", 2D, "sell-price", 1.5D, "max-quantity", 64)), mrSparkzz.nextMessage());
        assertEquals(-1, defaultStoreQuantity(Material.DIAMOND));
        assertEquals(64, Objects.requireNonNull(mrSparkzz.getInventory().getItem(0)).getAmount());
        printSuccessMessage("add command test - infinite stock for a new material");
    }

    @Test
    @DisplayName("Test Add - infinite stock - existing material without stock")
    void testAddCommand_InfiniteStock_ExistingMaterial() {
        Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(Material.EMERALD).put("quantity", 0);

        // any negative quantity is stored as -1
        performCommand(mrSparkzz, "shop add emerald -5");
        assertEquals(Notifier.compose(ADD_SUCCESS, Collections.singletonMap("material", Material.EMERALD)), mrSparkzz.nextMessage());
        assertEquals(-1, defaultStoreQuantity(Material.EMERALD));
        assertEquals(64, Objects.requireNonNull(mrSparkzz.getInventory().getItem(0)).getAmount());
        printSuccessMessage("add command test - infinite stock for an existing material");
    }

    @Test
    @DisplayName("Test Add - adding stock to an infinite material")
    void testAddCommand_AddToInfiniteStock() {
        Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(Material.EMERALD).put("quantity", -1);

        performCommand(mrSparkzz, "shop add emerald 5");
        assertEquals(Notifier.compose(ADD_INF_STOCK, null), mrSparkzz.nextMessage());
        assertEquals(-1, defaultStoreQuantity(Material.EMERALD));
        assertEquals(64, Objects.requireNonNull(mrSparkzz.getInventory().getItem(0)).getAmount());
        printSuccessMessage("add command test - adding stock to an infinite material");
    }

    @Test
    @DisplayName("Test Add - infinite stock - existing material with stock")
    void testAddCommand_InfiniteStock_ExistingMaterialWithStock() {
        performCommand(mrSparkzz, "shop add emerald -1");
        assertEquals(Notifier.compose(STORE_UPDATE_NO_STOCK, null), mrSparkzz.nextMessage());
        assertEquals(10, defaultStoreQuantity(Material.EMERALD));
        printSuccessMessage("add command test - infinite stock refused while the store has stock");
    }

    @Test
    @DisplayName("Test Add - non-op owner - infinite stock denied")
    void testAddCommand_NonOpOwner_InfiniteStockDenied() {
        useStoreOwnedByPlayer2(true);

        withPermission(player2, "shops.cmd.add", () -> performCommand(player2, "shop add emerald -1"));
        assertEquals(Notifier.compose(NO_PERMS_INF_STOCK, null), player2.nextMessage());
        assertEquals(0, defaultStoreQuantity(Material.EMERALD));
        printSuccessMessage("add command test - non-op owner denied infinite stock");
    }

    @Test
    @DisplayName("Test Add - non-op owner - new material with infinite stock denied")
    void testAddCommand_NonOpOwner_NewMaterialInfiniteStockDenied() {
        useStoreOwnedByPlayer2(false);

        withPermission(player2, "shops.cmd.add", () -> performCommand(player2, "shop add emerald 2 1.5 64 -1"));
        assertEquals(Notifier.compose(NO_PERMS_INF_STOCK, null), player2.nextMessage());
        assertFalse(Store.getDefaultStore(player2.getWorld()).get().containsMaterial(Material.EMERALD));
        printSuccessMessage("add command test - non-op owner denied infinite stock on a new material");
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
