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

@DisplayName("Buy Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BuyCommandTest {
    private static final ItemStack emeralds = new ItemStack(Material.EMERALD, 64);

    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST BUY COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        Shops.setDefaultShop(new Store("BetterBuy", mrSparkzz.getUniqueId()));
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @BeforeEach
    void setUpBuyCommand() {
        Shops.getDefaultShop().getItems().clear();
        Shops.getDefaultShop().addItem(emeralds.getType(), emeralds.getAmount(), -1, 2D, 1.5D);
        // TODO: Shops.getEconomy().depositPlayer(mrSparkzz, 50);
    }

    @Test
    @DisplayName("Test Buy - permissions")
    @Order(1)
    void testBuyCommand_Permissions() {
        performCommand(player2, "shop buy emerald 1");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("buy command permission check");
    }

    @Test
    @Disabled("Disabled until MockBukkit is updated to load plugins properly (or I find a new solution)")
    @DisplayName("Test Buy - main functionality - buy 100")
    @Order(2)
    void testBuyCommand() {
        int quantity = 1;

        performCommand(mrSparkzz, "shop buy " + quantity);
        assertEquals(String.format("%sYou have successfully deposited %s%s%s to the shop!", GREEN, GOLD, quantity, GREEN), mrSparkzz.nextMessage());
        assertEquals(25, Shops.getDefaultShop().getBalance());
        assertEquals(150, Shops.getEconomy().getBalance(mrSparkzz));
        printSuccessMessage("buy command test");
    }
}