package net.sparkzz.shops.command.sub;

import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.Config;
import net.sparkzz.shops.util.Notifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;
import java.util.Map;

import static net.sparkzz.shops.TestHelper.*;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GREEN;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Transfer Command")
class TransferCommandTest {

    private static PlayerMock mrSparkzz, player2;
    private static Store store;

    @BeforeAll
    static void setUpTransferCommand() {
        printMessage("==[ TEST TRANSFER COMMAND ]==");
        ServerMock server = startServer();

        mrSparkzz = server.addPlayer("MrSparkzz");
        player2 = server.addPlayer();

        mrSparkzz.setOp(true);
    }

    @AfterAll
    static void tearDown() {
        stopServer();
    }

    @BeforeEach
    void setUpShop() {
        Store.setDefaultStore(mrSparkzz.getWorld(), (store = new Store("BetterBuy", mrSparkzz.getUniqueId())));
    }

    @AfterEach
    void resetShop() {
        Store.DEFAULT_STORES.clear();
        Store.STORES.clear();
    }

    @Test
    @DisplayName("Test Transfer - permissions")
    void testTransferCommand_Permissions() {
        performCommand(player2, "shop transfer BetterBuy MrSparkzz");
        assertEquals(Notifier.compose(Notifier.CipherKey.NO_PERMS_CMD, null), player2.nextMessage());
        printSuccessMessage("transfer command permission check");
    }

    @Test
    @DisplayName("Test Transfer - main functionality")
    void testTransferCommand() {
        performCommand(mrSparkzz, String.format("shop transfer %s %s", store.getName(), player2.getName()));
        assertEquals(String.format("%sYou have successfully transferred %s%s%s to player %s%s%s!", GREEN, GOLD, store.getName(), GREEN, GOLD, player2.getName(), GREEN), mrSparkzz.nextMessage());
        assertEquals(player2.getUniqueId(), store.getOwner());
        printSuccessMessage("transfer command test");
    }

    @Test
    @DisplayName("Test Transfer - main functionality - multiple stores matched")
    void testTransferCommand_MultiStoreMatch() {
        Store duplicateStore = new Store("BetterBuy", mrSparkzz.getUniqueId());

        performCommand(mrSparkzz, String.format("shop transfer BetterBuy %s", player2.getName()));
        assertEquals("§cMultiple stores matched, please specify the store's UUID!", mrSparkzz.nextMessage());
        assertEquals(mrSparkzz.getUniqueId(), store.getOwner());
        assertEquals(mrSparkzz.getUniqueId(), duplicateStore.getOwner());
        printSuccessMessage("transfer command test - multiple stores matched");
    }

    @Test
    @DisplayName("Test Transfer - main functionality - no stores matched")
    void testTransferCommand_NoStoreMatch() {
        performCommand(mrSparkzz, String.format("shop transfer NoStore %s", player2.getName()));
        assertEquals("§cCould not find a store with the name and/or UUID of: §6NoStore§c!", mrSparkzz.nextMessage());
        assertEquals(mrSparkzz.getUniqueId(), store.getOwner());
        printSuccessMessage("transfer command test - no stores matched");
    }

    @Test
    @DisplayName("Test Transfer - main functionality - target player not found")
    void testTransferCommand_NoTargetPlayer() {
        performCommand(mrSparkzz, "shop transfer BetterBuy Player99");
        assertEquals("§cPlayer (Player99) not found!", mrSparkzz.nextMessage());
        assertEquals(mrSparkzz.getUniqueId(), store.getOwner());
        printSuccessMessage("transfer command test - target player not found");
    }

    @Test
    @DisplayName("Test Transfer - main functionality - target by UUID")
    void testTransferCommand_ByUUID() {
        performCommand(mrSparkzz, String.format("shop transfer %s %s", store.getName(), player2.getUniqueId()));
        assertEquals(String.format("%sYou have successfully transferred %s%s%s to player %s%s%s!", GREEN, GOLD, store.getName(), GREEN, GOLD, player2.getName(), GREEN), mrSparkzz.nextMessage());
        assertEquals(player2.getUniqueId(), store.getOwner());
        printSuccessMessage("transfer command test - target by UUID");
    }

    @Test
    @DisplayName("Test Transfer - not the owner")
    void testTransferCommand_NotOwner() {
        withPermission(player2, "shops.cmd.transfer", () -> performCommand(player2, String.format("shop transfer BetterBuy %s", player2.getName())));
        assertEquals(Notifier.compose(Notifier.CipherKey.NOT_OWNER, null), player2.nextMessage());
        assertEquals(mrSparkzz.getUniqueId(), store.getOwner());
        printSuccessMessage("transfer command test - not the owner");
    }

    @Test
    @DisplayName("Test Transfer - not the owner - admin override")
    void testTransferCommand_NotOwner_Override() {
        withPermissions(player2, List.of("shops.cmd.transfer", "shops.admin.override"), () -> performCommand(player2, String.format("shop transfer BetterBuy %s", player2.getName())));
        assertEquals(String.format("%sYou have successfully transferred %s%s%s to player %s%s%s!", GREEN, GOLD, store.getName(), GREEN, GOLD, player2.getName(), GREEN), player2.nextMessage());
        assertEquals(player2.getUniqueId(), store.getOwner());
        printSuccessMessage("transfer command test - not the owner with admin override");
    }

    @Test
    @DisplayName("Test Transfer - non-op owner - target at max stores")
    void testTransferCommand_NonOpOwner_TargetAtMaxStores() {
        Store player2Store = new Store("Player2Store", player2.getUniqueId());

        // mrSparkzz already owns BetterBuy, top them up to the limit
        for (int i = 1; i < Config.getMaxOwnedStores(); i++)
            new Store("SparkzzStore" + i, mrSparkzz.getUniqueId());

        withPermission(player2, "shops.cmd.transfer", () -> performCommand(player2, String.format("shop transfer Player2Store %s", mrSparkzz.getName())));
        assertEquals(Notifier.compose(Notifier.CipherKey.STORE_TRANSFER_FAIL_MAX_STORES, Map.of("target", mrSparkzz.getName(), "max-stores", Config.getMaxOwnedStores())), player2.nextMessage());
        assertEquals(player2.getUniqueId(), player2Store.getOwner());
        printSuccessMessage("transfer command test - non-op owner, target at max stores");
    }

    @Test
    @DisplayName("Test Transfer - non-op owner - target under max stores")
    void testTransferCommand_NonOpOwner_TargetUnderMaxStores() {
        Store player2Store = new Store("Player2Store", player2.getUniqueId());

        withPermission(player2, "shops.cmd.transfer", () -> performCommand(player2, String.format("shop transfer Player2Store %s", mrSparkzz.getName())));
        assertEquals(String.format("%sYou have successfully transferred %s%s%s to player %s%s%s!", GREEN, GOLD, "Player2Store", GREEN, GOLD, mrSparkzz.getName(), GREEN), player2.nextMessage());
        assertEquals(mrSparkzz.getUniqueId(), player2Store.getOwner());
        printSuccessMessage("transfer command test - non-op owner, target under max stores");
    }
}
