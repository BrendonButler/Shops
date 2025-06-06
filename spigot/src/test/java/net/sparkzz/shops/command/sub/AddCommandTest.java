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
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;

import static net.sparkzz.shops.TestHelper.*;
import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Add Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AddCommandTest {
    private static final ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUpAddCommand() {
        printMessage("==[ TEST ADD COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);
        loadConfig();

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Store.setDefaultStore(mrSparkzz.getWorld(), new Store("BetterBuy", mrSparkzz.getUniqueId()));
        Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().clear();
        Store.getDefaultStore(mrSparkzz.getWorld()).get().addItem(emeralds.getType(), 10, -1, 2D, 1.5D);
        Store.getDefaultStore(mrSparkzz.getWorld()).get().addFunds(BigDecimal.valueOf(100));
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        unLoadConfig();
        Store.DEFAULT_STORES.clear();
    }

    @BeforeEach
    void setUpEach() {
        mrSparkzz.getInventory().addItem(emeralds);
    }

    @AfterEach
    void tearDownEach() {
        Store.STORES.clear();
        mrSparkzz.getInventory().clear();
    }

    @Test
    @DisplayName("Test Add - permissions")
    @Order(1)
    void testAddCommand_Permissions() {
        performCommand(player2, "shop add acacia_log 1");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("add command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Add - main functionality - add 1")
    @Order(2)
    void testAddCommand_AddOne() {
        Material material = emeralds.getType();
        int quantity = emeralds.getAmount();

        performCommand(mrSparkzz, "shop add emerald 1");
        assertEquals(String.format("%sYou have successfully added %s%s%s to the shop!", GREEN, GOLD, (quantity > 0) ? String.valueOf(quantity) + GREEN + " of " + GOLD + material : material, GREEN), mrSparkzz.nextMessage());
        assertEquals(63, Objects.requireNonNull(mrSparkzz.getInventory().getItem(0)).getAmount());
        assertEquals(11, Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(material).get("quantity").intValue());
        printSuccessMessage("add command test - add 1");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Add - main functionality - add all")
    @Order(3)
    void testAddCommand_AddAll() {
        Material material = emeralds.getType();
        int quantity = emeralds.getAmount();

        performCommand(mrSparkzz, "shop add emerald all");
        assertEquals(String.format("%sYou have successfully added %s%s%s to the shop!", GREEN, GOLD, (quantity > 0) ? String.valueOf(quantity) + GREEN + " of " + GOLD + material : material, GREEN), mrSparkzz.nextMessage());
        assertFalse(mrSparkzz.getInventory().contains(material));
        assertEquals(11, Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(material).get("quantity").intValue());
        printSuccessMessage("add command test - add all");
    }

    @Test
    @DisplayName("Test Add - material not found in shop")
    @Order(4)
    void testAddCommand_NoMaterial() {
        mrSparkzz.getInventory().addItem(new ItemStack(Material.STICK, 64));
        performCommand(mrSparkzz, "shop add stick 1");
        assertEquals(Notifier.compose(MATERIAL_MISSING_STORE, Collections.singletonMap("material", Material.STICK)), mrSparkzz.nextMessage());
        printSuccessMessage("add command - material doesn't exist");
    }

    @Test
    @DisplayName("Test Add - invalid material")
    @Order(5)
    void testRemoveCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop add emeral 1");
        assertEquals(Notifier.compose(INVALID_MATERIAL, Collections.singletonMap("material", "emeral")), mrSparkzz.nextMessage());
        assertEquals("/shop [buy|sell|browse]", mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - invalid material");
    }

    @Test
    @DisplayName("Test Add - main functionality - no store")
    @Order(6)
    void testDepositCommand_NoStore() {
        Store.DEFAULT_STORES.clear();
        performCommand(mrSparkzz, "shop add emerald 1");
        assertEquals(Notifier.compose(NO_STORE_FOUND, null), mrSparkzz.nextMessage());
        printSuccessMessage("add command test - no store");
    }
}
