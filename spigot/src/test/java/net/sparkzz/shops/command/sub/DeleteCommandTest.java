package net.sparkzz.shops.command.sub;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.Material;
import org.bukkit.command.Command;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Delete Command")
class DeleteCommandTest {

    private static PlayerMock mrSparkzz, player2;
    private static ServerMock server;
    private static Store tempStore;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST DELETE COMMAND ]==");
        server = startServer();

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
        Store.setDefaultStore(mrSparkzz.getWorld(), (new Store("BetterBuy", mrSparkzz.getUniqueId())));
    }

    @AfterAll
    static void tearDown() {
        stopServer();
    }

    @BeforeEach
    void setUpStore() {
        tempStore = new Store("DollHairStore", player2.getUniqueId());
        setBalance(mrSparkzz, 0);
    }

    @AfterEach
    void tearDownStore() {
        Store.STORES.remove(tempStore);
        mrSparkzz.getInventory().clear();
    }

    @Test
    @DisplayName("Test Delete - permissions")
    void testDeleteShop_Permissions() {
        performCommand(player2, "shop delete DollHairStore");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("delete command permission check");
    }

    @Test
    @DisplayName("Test Delete - main functionality")
    void testDeleteShop() {
        performCommand(mrSparkzz, String.format("shop delete DollHairStore~%s", tempStore.getUUID()));
        assertEquals(String.format("%sYou have successfully deleted %s%s%s!", GREEN, GOLD, "DollHairStore", GREEN), mrSparkzz.nextMessage());
        printSuccessMessage("delete command test - deletion of DollHairStore");
    }

    @Test
    @DisplayName("Test Delete - main functionality - UUID")
    void testDeleteShop_ByUUID() {
        performCommand(mrSparkzz, String.format("shop delete %s", tempStore.getUUID()));
        assertEquals(String.format("%sYou have successfully deleted %s%s%s!", GREEN, GOLD, "DollHairStore", GREEN), mrSparkzz.nextMessage());
        printSuccessMessage(String.format("delete command test - deletion of DollHairStore by UUID (%s)", tempStore.getUUID()));
    }

    @Test
    @DisplayName("Test Delete - duplicate Store(s)")
    void testDeleteShop_DuplicateShops() {
        new Store("TestDuplicate");
        new Store("TestDuplicate");

        performCommand(mrSparkzz, "shop delete TestDuplicate");
        assertEquals(String.format("%sMultiple stores matched, please specify the store's UUID!", RED), mrSparkzz.nextMessage());
        printSuccessMessage("delete command test - duplicate shop identification on deletion");
    }

    @Test
    @DisplayName("Test Delete - store not found")
    void testDeleteShop_NotFound() {
        performCommand(mrSparkzz, "shop delete TestShop2");
        assertEquals(String.format("%sCould not find a store with the name and/or UUID of: %s%s%s!", RED, GOLD, "TestShop2", RED), mrSparkzz.nextMessage());
        printSuccessMessage("delete command test - no shop identification on deletion");
    }

    @Test
    @DisplayName("Test Delete - fail to delete")
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

    @Test
    @DisplayName("Test Delete - ignore stock")
    void testDeleteShop_IgnoreStock() {
        tempStore.addItem(new ItemStack(Material.EMERALD, 128));
        tempStore.addItem(new ItemStack(Material.BUCKET, 14));
        tempStore.setBalance(BigDecimal.valueOf(100));

        performCommand(mrSparkzz, "shop delete DollHairStore -f");
        assertTrue(mrSparkzz.getInventory().isEmpty());
        assertEquals(100, Shops.getEconomy().getBalance(mrSparkzz));
        assertEquals("§aYou have successfully deleted §6DollHairStore§a!", mrSparkzz.nextMessage());
        printSuccessMessage("delete command test - ignore stock");
    }

    @Test
    @DisplayName("Test Delete - ignore stock and funds")
    void testDeleteShop_IgnoreStockAndFunds() {
        tempStore.addItem(new ItemStack(Material.EMERALD, 128));
        tempStore.addItem(new ItemStack(Material.BUCKET, 14));
        tempStore.setBalance(BigDecimal.valueOf(100));

        performCommand(mrSparkzz, "shop delete DollHairStore -F");
        assertTrue(mrSparkzz.getInventory().isEmpty());
        assertEquals(0, Shops.getEconomy().getBalance(mrSparkzz));
        assertEquals("§aYou have successfully deleted §6DollHairStore§a!", mrSparkzz.nextMessage());
        printSuccessMessage("delete command test - ignore stock and funds");
    }

    @Test
    @DisplayName("Test Delete - not the owner")
    void testDeleteShop_NotOwner() {
        Store ownedStore = new Store("SparkzzOnly", mrSparkzz.getUniqueId());

        withPermission(player2, "shops.cmd.delete", () -> performCommand(player2, "shop delete SparkzzOnly"));
        assertEquals(Notifier.compose(Notifier.CipherKey.NOT_OWNER, null), player2.nextMessage());
        assertTrue(Store.STORES.contains(ownedStore));
        Store.STORES.remove(ownedStore);
        printSuccessMessage("delete command test - not the owner");
    }

    @Test
    @DisplayName("Test Delete - not the owner - admin override")
    void testDeleteShop_NotOwner_Override() {
        Store ownedStore = new Store("SparkzzOnly", mrSparkzz.getUniqueId());

        withPermissions(player2, List.of("shops.cmd.delete", "shops.admin.override"), () -> performCommand(player2, "shop delete SparkzzOnly"));
        assertEquals(String.format("%sYou have successfully deleted %s%s%s!", GREEN, GOLD, "SparkzzOnly", GREEN), player2.nextMessage());
        assertFalse(Store.STORES.contains(ownedStore));
        printSuccessMessage("delete command test - not the owner with admin override");
    }

    @Test
    @DisplayName("Test Delete - non-op owner")
    void testDeleteShop_NonOpOwner() {
        withPermission(player2, "shops.cmd.delete", () -> performCommand(player2, "shop delete DollHairStore"));
        assertEquals(String.format("%sYou have successfully deleted %s%s%s!", GREEN, GOLD, "DollHairStore", GREEN), player2.nextMessage());
        assertFalse(Store.STORES.contains(tempStore));
        printSuccessMessage("delete command test - non-op owner");
    }

    @Test
    @DisplayName("Test Delete - insufficient inventory space")
    void testDeleteShop_InsufficientInventory() {
        tempStore.addItem(new ItemStack(Material.EMERALD, 128));
        mrSparkzz.getInventory().addItem(new ItemStack(Material.MILK_BUCKET, 2304));

        performCommand(mrSparkzz, "shop delete DollHairStore");
        assertEquals(Notifier.compose(Notifier.CipherKey.STORE_DELETE_INSUFFICIENT_INV_PLAYER, null), mrSparkzz.nextMessage());
        assertTrue(Store.STORES.contains(tempStore));
        printSuccessMessage("delete command test - insufficient inventory space");
    }

    @Test
    @DisplayName("Test Delete - unknown flag is ignored")
    void testDeleteShop_UnknownFlag() {
        tempStore.addItem(new ItemStack(Material.EMERALD, 10));

        performCommand(mrSparkzz, "shop delete DollHairStore -x");
        assertEquals(String.format("%sYou have successfully deleted %s%s%s!", GREEN, GOLD, "DollHairStore", GREEN), mrSparkzz.nextMessage());
        assertEquals(10, Objects.requireNonNull(mrSparkzz.getInventory().getItem(0)).getAmount());
        printSuccessMessage("delete command test - unknown flag is ignored");
    }
}
