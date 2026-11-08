package net.sparkzz.shops.command;

import net.sparkzz.shops.Store;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Optional;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("SpellCheckingInspection")
@DisplayName("Sub Command")
class SubCommandTest {

    private static Store store;
    private static SubCommandTestClass testClass;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST SUB COMMAND ]==");
        ServerMock server = startServer();
        testClass = new SubCommandTestClass();

        PlayerMock mrSparkzz = server.addPlayer("MrSparkzz");

        mrSparkzz.setOp(true);
        Store.setDefaultStore(mrSparkzz.getWorld(), store = new Store("BetterBuy", mrSparkzz.getUniqueId()));
    }

    @AfterAll
    static void tearDown() {
        stopServer();
    }

    @Test
    @DisplayName("Test identify store - name")
    void testIdentifyStore_Name() {
        Optional<Store> identifiedStore = testClass.identifyStore(store.getName());
        assertEquals(store, identifiedStore.orElse(null));
        printSuccessMessage("identify store - name");
    }

    @Test
    @DisplayName("Test identify store - UUID")
    void testIdentifyStore_UUID() {
        Optional<Store> identifiedStore = testClass.identifyStore(store.getUUID().toString());
        assertEquals(store, identifiedStore.orElse(null));
        printSuccessMessage("identify store - UUID");
    }

    @Test
    @DisplayName("Test identify store - name~UUID")
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
