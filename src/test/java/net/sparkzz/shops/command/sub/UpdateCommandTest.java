package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Update Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UpdateCommandTest {

    private static boolean wasInfStock, wasInfFunds;
    private static PlayerMock mrSparkzz, player2;
    private static String oldName;

    @BeforeAll
    static void saveOldValues() {
        printMessage("==[ TEST UPDATE COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Shops.setDefaultShop(new Store("BetterBuy", mrSparkzz.getUniqueId()));
        oldName = Shops.getDefaultShop().getName();
        wasInfFunds = Shops.getDefaultShop().hasInfiniteFunds();
        wasInfStock = Shops.getDefaultShop().hasInfiniteStock();
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @AfterEach
    void resetShop() {
        Shops.getDefaultShop().setName(oldName);
        Shops.getDefaultShop().setInfiniteFunds(wasInfFunds);
        Shops.getDefaultShop().setInfiniteStock(wasInfStock);
    }

    @Test
    @DisplayName("Test Update - permissions")
    @Order(1)
    void testUpdateCommand_Permissions() {
        performCommand(player2, "shop update shop-name TestShop99");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("update command permission check");
    }

    @Test
    @DisplayName("Test Update - main functionality - infinite-funds true")
    @Order(2)
    void testUpdateCommand_InfFunds() {
        performCommand(mrSparkzz, "shop update infinite-funds true");
        assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in the shop!", GREEN, GOLD, "infinite-funds", GREEN, GOLD, "true", GREEN), mrSparkzz.nextMessage());
        assertTrue(Shops.getDefaultShop().hasInfiniteFunds());
        printSuccessMessage("update command test - update infinite funds");
    }

    @Test
    @DisplayName("Test Update - main functionality - inf-stock true")
    @Order(2)
    void testUpdateCommand_InfStock() {
        performCommand(mrSparkzz, "shop update infinite-stock true");
        assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in the shop!", GREEN, GOLD, "infinite-stock", GREEN, GOLD, "true", GREEN), mrSparkzz.nextMessage());
        assertTrue(Shops.getDefaultShop().hasInfiniteStock());
        printSuccessMessage("update command test - update infinite stock");
    }

    @Test
    @DisplayName("Test Update - main functionality - shop name")
    @Order(2)
    void testUpdateCommand_ShopName() {
        performCommand(mrSparkzz, "shop update shop-name TestShop99");
        assertEquals(String.format("%sYou have successfully updated %s%s%s to %s%s%s in the shop!", GREEN, GOLD, "shop-name", GREEN, GOLD, "TestShop99", GREEN), mrSparkzz.nextMessage());
        assertEquals("TestShop99", Shops.getDefaultShop().getName());
        printSuccessMessage("update command test - update shop name");
    }
}
