package net.sparkzz.shops.command.sub;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.command.sub.DeleteCommand;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.command.Command;
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

import java.util.Optional;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Delete Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeleteCommandTest {

    private static PlayerMock mrSparkzz, player2;
    private static ServerMock server;
    private static Store tempStore;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST DELETE COMMAND ]==");
        server = MockBukkit.getOrCreateMock();

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
    void setUpStore() {
        tempStore = new Store("DollHairStore", player2.getUniqueId());
    }

    @AfterEach
    void tearDownStore() {
        Store.STORES.remove(tempStore);
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
        performCommand(mrSparkzz, String.format("shop delete DollHairStore~%s", tempStore.getUUID()));
        assertEquals(String.format("%sYou have successfully deleted %s%s%s!", GREEN, GOLD, "DollHairStore", GREEN), mrSparkzz.nextMessage());
        printSuccessMessage("delete command test - deletion of DollHairStore");
    }

    @Test
    @DisplayName("Test Delete - main functionality - UUID")
    @Order(2)
    void testDeleteShop_ByUUID() {
        performCommand(mrSparkzz, String.format("shop delete %s", tempStore.getUUID()));
        assertEquals(String.format("%sYou have successfully deleted %s%s%s!", GREEN, GOLD, "DollHairStore", GREEN), mrSparkzz.nextMessage());
        printSuccessMessage(String.format("delete command test - deletion of DollHairStore by UUID (%s)", tempStore.getUUID()));
    }

    @Test
    @DisplayName("Test Delete - duplicate Store(s)")
    @Order(3)
    void testDeleteShop_DuplicateShops() {
        new Store("TestDuplicate");
        new Store("TestDuplicate");

        performCommand(mrSparkzz, "shop delete TestDuplicate");
        assertEquals(String.format("%sMultiple stores matched, please specify the store's UUID!", RED), mrSparkzz.nextMessage());
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

    @Test
    @DisplayName("Test Delete - fail to delete")
    @Order(5)
    void testDeleteShop_Fail() {
        Command command = server.getCommandMap().getCommand("shop");
        Store store = new Store("FailStore");

        // remove store before it can be removed by the command
        Store.STORES.remove(store);

        // Override identifyStore to return a store that's not within the list
        DeleteCommand deleteCommand = new DeleteCommand() {
            @Override
            protected Optional<Store> identifyStore(String nameOrUUID) {
                return Optional.of(store);
            }
        };

        deleteCommand.process(mrSparkzz, command, "shop", new String[]{ "delete", "TestStore" });
        assertEquals("§cSomething went wrong when attempting to delete the store!", mrSparkzz.nextMessage());
        printSuccessMessage("delete command test - fail to delete shop");
    }
}
