package net.sparkzz.event;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.mocks.MockVault;
import net.sparkzz.util.Cuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.printMessage;
import static net.sparkzz.shops.TestHelper.printSuccessMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Entrance Listener")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntranceListenerTest {

    private static Location home, inStore;
    private static PlayerMock mrSparkzz;
    private static World otherWorld;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST ENTRANCE LISTENER ]==");
        ServerMock server = MockBukkit.getOrCreateMock();

        MockBukkit.loadWith(MockVault.class, new PluginDescriptionFile("Vault", "MOCK", "net.sparkzz.shops.mocks.MockVault"));
        MockBukkit.load(Shops.class);
        World world = server.createWorld(WorldCreator.name("main-world"));
        otherWorld = server.createWorld(WorldCreator.name("other-world"));
        home = new Location(world, 0D, 0D, 0D);
        inStore = new Location(world, 15D, 15D, 15D);

        Shops.setMockServer(server);
        mrSparkzz = server.addPlayer("MrSparkzz");

        mrSparkzz.setLocation(new Location(world, 0D, 0D, 0D));
        Store.setDefaultStore(new Store("BetterBuy", mrSparkzz.getUniqueId(), new Cuboid(world, 10D, 10D, 10D, 20D, 20D, 20D)));
        Store.setDefaultStore(new Store("WorstBuy", mrSparkzz.getUniqueId(), new Cuboid(null, 10D, 10D, 10D, 20D, 20D, 20D)));
    }

    @AfterAll
    static void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
        Store.setDefaultStore(null);
        Store.STORES.clear();
    }

    @BeforeEach
    void resetLocationAndMessages() {
        Location location = home.clone();

        location.setPitch(location.getPitch() + 1);
        mrSparkzz.setLocation(home);
        mrSparkzz.simulatePlayerMove(location);

        while (mrSparkzz.nextMessage() != null)
            mrSparkzz.nextMessage();
    }

    @Test
    @DisplayName("Test Player entering store")
    @Order(1)
    void testPlayerEnteringStore() {
        mrSparkzz.simulatePlayerMove(inStore);
        assertEquals("§9Welcome to §6BetterBuy§9!", mrSparkzz.nextMessage());
        printSuccessMessage("enter store test");
    }

    @Test
    @DisplayName("Test Player exiting store")
    @Order(2)
    void testPlayerExitingStore() {
        Location location = inStore.clone();
        location.setPitch(location.getPitch() + 1);

        mrSparkzz.setLocation(inStore);
        mrSparkzz.simulatePlayerMove(location);
        mrSparkzz.nextMessage();
        mrSparkzz.simulatePlayerMove(home);
        assertEquals("§9We hope to see you again!", mrSparkzz.nextMessage());
        printSuccessMessage("exit store test");
    }

    @Test
    @DisplayName("Test Player entering and exiting store")
    @Order(3)
    void testPlayerEnteringAndExitingStore() {
        mrSparkzz.simulatePlayerMove(inStore);
        assertEquals("§9Welcome to §6BetterBuy§9!", mrSparkzz.nextMessage());
        mrSparkzz.simulatePlayerMove(home);
        assertEquals("§9We hope to see you again!", mrSparkzz.nextMessage());
        printSuccessMessage("enter and exit store test");
    }

    @Test
    @DisplayName("Test Player exiting and entering store")
    @Order(4)
    void testPlayerExitingAndEnteringStore() {
        mrSparkzz.setLocation(inStore);
        mrSparkzz.simulatePlayerMove(inStore.add(1, 1, 1));
        mrSparkzz.nextMessage();
        mrSparkzz.simulatePlayerMove(home);
        assertEquals("§9We hope to see you again!", mrSparkzz.nextMessage());
        mrSparkzz.simulatePlayerMove(inStore);
        assertEquals("§9Welcome to §6BetterBuy§9!", mrSparkzz.nextMessage());
        printSuccessMessage("exit and enter store test");
    }

    @Test
    @DisplayName("Test Player entering store - wrong world")
    @Order(5)
    void testPlayerEnteringStore_WrongWorld() {
        Location otherWorldHome = home.clone();
        Location otherWorldInStore = inStore.clone();

        otherWorldHome.setWorld(otherWorld);
        otherWorldInStore.setWorld(otherWorld);
        mrSparkzz.setLocation(otherWorldHome);
        mrSparkzz.simulatePlayerMove(otherWorldInStore);
        assertNull(mrSparkzz.nextMessage());
        printSuccessMessage("enter store test - wrong world");
    }

    @Test
    @DisplayName("Test Player exiting store - wrong world")
    @Order(6)
    void testPlayerExitingStore_WrongWorld() {
        Location otherWorldHome = home.clone();
        Location otherWorldInStore = inStore.clone();

        otherWorldHome.setWorld(otherWorld);
        otherWorldInStore.setWorld(otherWorld);
        mrSparkzz.setLocation(otherWorldInStore);
        mrSparkzz.simulatePlayerMove(otherWorldHome);
        assertNull(mrSparkzz.nextMessage());
        printSuccessMessage("exit store test - wrong world");
    }

    @Test
    @DisplayName("Test Player moving around in the store")
    @Order(7)
    void testPlayerWalkingAroundInStore() {
        mrSparkzz.setLocation(inStore);
        mrSparkzz.simulatePlayerMove(inStore.add(1, 1, 1));
        mrSparkzz.nextMessage();
        assertNull(mrSparkzz.nextMessage());
        printSuccessMessage("walking around in store test");
    }

    @Test
    @DisplayName("Test Player moving around out of the store")
    @Order(8)
    void testPlayerWalkingAroundOutOfStore() {
        mrSparkzz.setLocation(home);
        mrSparkzz.simulatePlayerMove(home.add(1, 1, 1));
        assertNull(mrSparkzz.nextMessage());
        printSuccessMessage("walking around out of store test");
    }
}
