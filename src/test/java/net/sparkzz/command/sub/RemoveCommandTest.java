package net.sparkzz.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Objects;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
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
        Store.setDefaultStore(mrSparkzz.getWorld(), new Store("BetterBuy", mrSparkzz.getUniqueId()));
        Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().clear();
        Store.getDefaultStore(mrSparkzz.getWorld()).get().addItem(emeralds.getType(), 0, -1, 2D, 1.5D);
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        unLoadConfig();
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
    }

    @Test
    @DisplayName("Test Remove - permissions")
    @Order(1)
    void testRemoveCommand_Permissions() {
        performCommand(player2, "shop remove acacia_log 1");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("remove command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Remove - main functionality - remove 1")
    @Order(2)
    void testRemoveCommand_RemoveOne() {
        Material material = emeralds.getType();

        performCommand(mrSparkzz, "shop remove emerald 1");
        assertEquals(String.format("%sYou have successfully removed %s%s%s from the shop!", GREEN, GOLD, material, GREEN), mrSparkzz.nextMessage());
        assertEquals(63, Objects.requireNonNull(mrSparkzz.getInventory().getItem(0)).getAmount());
        assertEquals(11, Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(material).get("quantity").intValue());
        printSuccessMessage("remove command test - remove 1 of type from shop");
    }

    @Test
    @DisplayName("Test Remove - main functionality - remove all")
    @Order(3)
    void testRemoveCommand_RemoveAll() {
        Material material = emeralds.getType();

        performCommand(mrSparkzz, "shop remove emerald");
        assertEquals(String.format("%sYou have successfully removed %s%s%s from the store!", GREEN, GOLD, material, GREEN), mrSparkzz.nextMessage());
        assertNull(Store.getDefaultStore(mrSparkzz.getWorld()).get().getItems().get(material));
        printSuccessMessage("remove command test - remove all of type from shop");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Remove - material not found in shop")
    @Order(4)
    void testRemoveCommand_NoMaterial() {
        performCommand(mrSparkzz, "shop remove emerald 1");
        assertEquals(String.format("%sThis material doesn't currently exist in the shop, use `/shop add %s` to add this item", RED, Material.EMERALD), mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - material doesn't exist");
    }

    @Test
    @DisplayName("Test Remove - invalid material")
    @Order(5)
    void testRemoveCommand_InvalidMaterial() {
        performCommand(mrSparkzz, "shop remove emeral 1");
        assertEquals("§cInvalid material (emeral)!", mrSparkzz.nextMessage());
        printSuccessMessage("remove command test - invalid material");
    }
}
