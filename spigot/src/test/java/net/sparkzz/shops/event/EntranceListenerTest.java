package net.sparkzz.shops.event;

import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.Cuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.simulate.entity.PlayerSimulation;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Entrance Listener")
public class EntranceListenerTest {

    private static Location home, inStore;
    private static PlayerMock mrSparkzz;
    private static PlayerSimulation simulation;
    private static World otherWorld;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST ENTRANCE LISTENER ]==");
        ServerMock server = startServer();

        World world = server.createWorld(WorldCreator.name("main-world"));
        otherWorld = server.createWorld(WorldCreator.name("other-world"));
        home = new Location(world, 0D, 0D, 0D);
        inStore = new Location(world, 15D, 15D, 15D);

        mrSparkzz = server.addPlayer("MrSparkzz");
        simulation = new PlayerSimulation(mrSparkzz);

        mrSparkzz.setLocation(new Location(world, 0D, 0D, 0D));
        Store.setDefaultStore(mrSparkzz.getWorld(), new Store("BetterBuy", mrSparkzz.getUniqueId(), new Cuboid(world, 10D, 10D, 10D, 20D, 20D, 20D)));
        new Store("WorstBuy", mrSparkzz.getUniqueId(), new Cuboid(null, 10D, 10D, 10D, 20D, 20D, 20D));
    }

    @AfterAll
    static void tearDown() {
        stopServer();
    }

    @BeforeEach
    void resetLocationAndMessages() {
        Location location = home.clone();

        location.setPitch(location.getPitch() + 1);
        mrSparkzz.setLocation(home);
        simulation.simulatePlayerMove(location);

        while (mrSparkzz.nextMessage() != null)
            mrSparkzz.nextMessage();
    }

    @Test
    @DisplayName("Test Player entering store")
    void testPlayerEnteringStore() {
        simulation.simulatePlayerMove(inStore);
        assertEquals("§9Welcome to §6BetterBuy§9!", mrSparkzz.nextMessage());
        printSuccessMessage("enter store test");
    }

    @Test
    @DisplayName("Test Player exiting store")
    void testPlayerExitingStore() {
        Location location = inStore.clone();
        location.setPitch(location.getPitch() + 1);

        mrSparkzz.setLocation(inStore);
        simulation.simulatePlayerMove(location);
        mrSparkzz.nextMessage();
        simulation.simulatePlayerMove(home);
        assertEquals("§9We hope to see you again!", mrSparkzz.nextMessage());
        printSuccessMessage("exit store test");
    }

    @Test
    @DisplayName("Test Player entering and exiting store")
    void testPlayerEnteringAndExitingStore() {
        simulation.simulatePlayerMove(inStore);
        assertEquals("§9Welcome to §6BetterBuy§9!", mrSparkzz.nextMessage());
        simulation.simulatePlayerMove(home);
        assertEquals("§9We hope to see you again!", mrSparkzz.nextMessage());
        printSuccessMessage("enter and exit store test");
    }

    @Test
    @DisplayName("Test Player exiting and entering store")
    void testPlayerExitingAndEnteringStore() {
        mrSparkzz.setLocation(inStore);
        simulation.simulatePlayerMove(inStore.add(1, 1, 1));
        mrSparkzz.nextMessage();
        simulation.simulatePlayerMove(home);
        assertEquals("§9We hope to see you again!", mrSparkzz.nextMessage());
        simulation.simulatePlayerMove(inStore);
        assertEquals("§9Welcome to §6BetterBuy§9!", mrSparkzz.nextMessage());
        printSuccessMessage("exit and enter store test");
    }

    @Test
    @DisplayName("Test Player entering store - wrong world")
    void testPlayerEnteringStore_WrongWorld() {
        Location otherWorldHome = home.clone();
        Location otherWorldInStore = inStore.clone();

        otherWorldHome.setWorld(otherWorld);
        otherWorldInStore.setWorld(otherWorld);
        mrSparkzz.setLocation(otherWorldHome);
        simulation.simulatePlayerMove(otherWorldInStore);
        assertNull(mrSparkzz.nextMessage());
        printSuccessMessage("enter store test - wrong world");
    }

    @Test
    @DisplayName("Test Player exiting store - wrong world")
    void testPlayerExitingStore_WrongWorld() {
        Location otherWorldHome = home.clone();
        Location otherWorldInStore = inStore.clone();

        otherWorldHome.setWorld(otherWorld);
        otherWorldInStore.setWorld(otherWorld);
        mrSparkzz.setLocation(otherWorldInStore);
        simulation.simulatePlayerMove(otherWorldHome);
        assertNull(mrSparkzz.nextMessage());
        printSuccessMessage("exit store test - wrong world");
    }

    @Test
    @DisplayName("Test Player moving around in the store")
    void testPlayerWalkingAroundInStore() {
        mrSparkzz.setLocation(inStore);
        simulation.simulatePlayerMove(inStore.add(1, 1, 1));
        mrSparkzz.nextMessage();
        assertNull(mrSparkzz.nextMessage());
        printSuccessMessage("walking around in store test");
    }

    @Test
    @DisplayName("Test Player moving around out of the store")
    void testPlayerWalkingAroundOutOfStore() {
        mrSparkzz.setLocation(home);
        simulation.simulatePlayerMove(home.add(1, 1, 1));
        assertNull(mrSparkzz.nextMessage());
        printSuccessMessage("walking around out of store test");
    }
}
