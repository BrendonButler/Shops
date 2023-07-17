package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Delete Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeleteCommandTest {

    private static PlayerMock mrSparkzz, player2;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST DELETE COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Shops.setDefaultShop(new Store("BetterBuy", mrSparkzz.getUniqueId()));
        new Store("DollHairStore", player2.getUniqueId());
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Test Delete - permissions")
    @Order(1)
    void testDeleteShop_Permissions() {
        performCommand(player2, "shop delete DollHairStore");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("delete command permission check");
    }

    @Test
    @DisplayName("Test Delete - main functionality")
    @Order(2)
    void testDeleteShop() {
        performCommand(mrSparkzz, "shop delete DollHairStore");
        assertEquals(String.format("%sYou have successfully deleted %s%s%s!", GREEN, GOLD, "DollHairStore", GREEN), mrSparkzz.nextMessage());
        printSuccessMessage("delete command test - deletion of DollHairStore");
    }

    @Test
    @DisplayName("Test Delete - duplicate Store(s)")
    @Order(3)
    void testDeleteShop_DuplicateShops() {
        new Store("TestDuplicate");
        new Store("TestDuplicate");

        performCommand(mrSparkzz, "shop delete TestDuplicate");
        assertEquals(String.format("%sMultiple shops matched, please specify the shop's UUID!", RED), mrSparkzz.nextMessage());
        printSuccessMessage("delete command test - duplicate shop identification on deletion");
    }

    @Test
    @DisplayName("Test Delete - store not found")
    @Order(4)
    void testDeleteShop_NotFound() {
        performCommand(mrSparkzz, "shop delete TestShop2");
        assertEquals(String.format("%sCould not find a store with the name and/or UUID of: %s%s%s!", RED, GOLD, "TestShop2", RED), mrSparkzz.nextMessage());
        printSuccessMessage("delete command test - no shop identification on deletion");
    }
}
