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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Transfer Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransferCommandTest {

    private static PlayerMock mrSparkzz, player2;
    private static Store store, duplicateStore;

    @BeforeAll
    static void setUpTransferCommand() {
        printMessage("==[ TEST TRANSFER COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        Shops.setMockServer(server);
        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @BeforeEach
    void setUpShop() {
        Shops.setDefaultShop((store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
    }

    @AfterEach
    void resetShop() {
        store.setOwner(mrSparkzz.getUniqueId());
        Store.STORES.clear();
    }

    @Test
    @DisplayName("Test Transfer - permissions")
    @Order(1)
    void testTransferCommand_Permissions() {
        performCommand(player2, "shop transfer BetterBuy MrSparkzz");
        assertEquals(String.format("%sYou do not have permission to use this command!", RED), player2.nextMessage());
        printSuccessMessage("transfer command permission check");
    }

    @Test
    //@Disabled("Not sure exactly what's going on with this, but it's having trouble loading either the Server or Player (TransferSubCommand:44)")
    @DisplayName("Test Transfer - main functionality")
    @Order(2)
    void testTransferCommand() {
        performCommand(mrSparkzz, String.format("shop transfer %s %s", store.getName(), player2.getName()));
        assertEquals(String.format("%sYou have successfully transferred %s%s%s to player %s%s%s!", GREEN, GOLD, store.getName(), GREEN, GOLD, player2.getName(), GREEN), mrSparkzz.nextMessage());
        assertEquals(player2.getUniqueId(), store.getOwner());
        printSuccessMessage("transfer command test");
    }

    @Test
    @DisplayName("Test Transfer - main functionality - multiple stores matched")
    @Order(3)
    void testTransferCommand_MultiStoreMatch() {
        duplicateStore = new Store("BetterBuy", mrSparkzz.getUniqueId());

        performCommand(mrSparkzz, String.format("shop transfer BetterBuy %s", player2.getName()));
        assertEquals("§cMultiple stores matched, please specify the store's UUID!", mrSparkzz.nextMessage());
        assertEquals(mrSparkzz.getUniqueId(), store.getOwner());
        assertEquals(mrSparkzz.getUniqueId(), duplicateStore.getOwner());
        printSuccessMessage("transfer command test - multiple stores matched");
    }

    @Test
    @DisplayName("Test Transfer - main functionality - no stores matched")
    @Order(4)
    void testTransferCommand_NoStoreMatch() {
        performCommand(mrSparkzz, String.format("shop transfer NoStore %s", player2.getName()));
        assertEquals("§cCould not find a store with the name and/or UUID of: §6NoStore§c!", mrSparkzz.nextMessage());
        assertEquals(mrSparkzz.getUniqueId(), store.getOwner());
        assertEquals(mrSparkzz.getUniqueId(), duplicateStore.getOwner());
        printSuccessMessage("transfer command test - no stores matched");
    }

    @Test
    @DisplayName("Test Transfer - main functionality - target player not found")
    @Order(5)
    void testTransferCommand_NoTargetPlayer() {
        performCommand(mrSparkzz, "shop transfer BetterBuy Player99");
        assertEquals("§aPlayer (Player99) not found!", mrSparkzz.nextMessage());
        assertEquals(mrSparkzz.getUniqueId(), store.getOwner());
        assertEquals(mrSparkzz.getUniqueId(), duplicateStore.getOwner());
        printSuccessMessage("transfer command test - target player not found");
    }
}
