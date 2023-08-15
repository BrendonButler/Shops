package net.sparkzz.command;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Optional;

import static net.sparkzz.shops.TestHelper.printMessage;
import static net.sparkzz.shops.TestHelper.printSuccessMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Sub Command")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SubCommandTest {

    private static Store store;
    private static SubCommandTestClass testClass;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST SUB COMMAND ]==");
        ServerMock server = MockBukkit.getOrCreateMock();
        testClass = new SubCommandTestClass();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);

        PlayerMock mrSparkzz = server.addPlayer("MrSparkzz");

        mrSparkzz.setOp(true);
        Store.setDefaultStore(store = new Store("BetterBuy", mrSparkzz.getUniqueId()));
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
        Store.STORES.clear();
    }

    @Test
    @DisplayName("Test identify store - name")
    @Order(1)
    void testIdentifyStore_Name() {
        Optional<Store> identifiedStore = testClass.identifyStore(store.getName());
        assertEquals(store, identifiedStore.orElse(null));
        printSuccessMessage("identify store - name");
    }

    @Test
    @DisplayName("Test identify store - UUID")
    @Order(2)
    void testIdentifyStore_UUID() {
        Optional<Store> identifiedStore = testClass.identifyStore(store.getUUID().toString());
        assertEquals(store, identifiedStore.orElse(null));
        printSuccessMessage("identify store - UUID");
    }

    @Test
    @DisplayName("Test identify store - name~UUID")
    @Order(3)
    void testIdentifyStore_NameAndUUID() {
        Optional<Store> identifiedStore = testClass.identifyStore(String.format("%s~%s", store.getName(), store.getUUID()));
        assertEquals(store, identifiedStore.orElse(null));
        printSuccessMessage("identify store - UUID");
    }

    private static class SubCommandTestClass extends SubCommand {

        @Override
        public boolean process(CommandSender sender, Command command, String label, String[] args) {
            return false;
        }

        @Override
        public Optional<Store> identifyStore(String name) {
            return super.identifyStore(name);
        }
    }
}
