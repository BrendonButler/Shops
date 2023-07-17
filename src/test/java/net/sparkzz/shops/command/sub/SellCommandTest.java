package net.sparkzz.shops.command.sub;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Sell Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SellCommandTest {
    private static final ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST SELL COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Shops.setDefaultShop(new Store("BetterBuy", mrSparkzz.getUniqueId()));
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @BeforeEach
    void setUpSellCommand() {
        Shops.getDefaultShop().getItems().clear();
        Shops.getDefaultShop().addItem(emeralds.getType(), 0, -1, 2D, 1.5D);
        Shops.getDefaultShop().addFunds(100);
        mrSparkzz.getInventory().addItem(emeralds);
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
    }

    @Test
    @DisplayName("Test Sell - permissions")
    @Order(1)
    void testSellCommand_Permissions() {
        performCommand(player2, "shop sell emerald 1");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("sell command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Sell - main functionality - sell 1")
    @Order(2)
    void testSellCommand() {
        Material material = emeralds.getType();
        int quantity = 1;
        double price = Shops.getDefaultShop().getSellPrice(material);

        performCommand(mrSparkzz, "shop sell emerald " + quantity);
        assertEquals(String.format("%sSuccess! You have sold %s%s%s of %s%s%s for %s$%.2f%s.",
                GREEN, GOLD, quantity, GREEN, GOLD, material, GREEN, GOLD, price * quantity, GREEN), mrSparkzz.nextMessage());
        assertEquals(25, Shops.getDefaultShop().getBalance());
        assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("sell command test");
    }
}
