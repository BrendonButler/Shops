package net.sparkzz.shops.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static net.sparkzz.shops.TestHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Config Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConfigTest {

    private static ServerMock server;
    private static Store store, otherStore;
    private static World world, world_nether, world_the_end;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST CONFIG ]==");
        server = MockBukkit.getOrCreateMock();
        MockBukkit.load(Shops.class);
        loadConfig();
        world = server.createWorld(WorldCreator.name("world"));
        world_nether = server.createWorld(WorldCreator.name("world_nether"));
        world_the_end = server.createWorld(WorldCreator.name("world_the_end"));
        store = new Store("TestStore");
        otherStore = new Store("TestStore2");
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        List<Cuboid> cuboids = List.of(
                new Cuboid(world, -20, -64, -20, 20, 320, 20),
                new Cuboid(world_nether, -20, -64, -20, 20, 128, 20),
                new Cuboid(world_the_end, -20, -64, -20, 20, 256, 20)
        );

        Config.setMinDimensions(3D, 3D, 3D);
        Config.setMaxDimensions(8D, 16D, 8D);
        Config.setMinVolume(27D);
        Config.setMaxVolume(1024D);
        Config.setOffLimitsAreas(cuboids);
        Config.setMaxOwnedStores(2);
        unLoadConfig();
        Store.STORES.clear();
        Store.DEFAULT_STORES.clear();
    }

    @AfterEach
    void resetConfig() {
        Config.setDefaultStore(null, null);
    }

    @Test
    @DisplayName("Test get root node children")
    @Order(1)
    void testGetRootNodeChildren() {
        assertEquals("[store, messages]", Config.getRootNode().childrenMap().keySet().toString());
        printSuccessMessage("getting root node");
    }

    @Test
    @DisplayName("Test set minimum dimensions")
    @Order(2)
    void testSetMinDimensions() {
        Config.setMinDimensions(2.5D, 1.75D, 3.2D);
        double[] coordinates = Config.getMinDimensions();
        assertEquals(2.5D, coordinates[0]);
        assertEquals(1.75D, coordinates[1]);
        assertEquals(3.2D, coordinates[2]);
        printSuccessMessage("setting minimum dimensions");
    }

    @Test
    @DisplayName("Test set maximum dimensions")
    @Order(3)
    void testSetMaxDimensions() {
        Config.setMaxDimensions(5.95D, 15D, 7.25D);
        double[] coordinates = Config.getMaxDimensions();
        assertEquals(5.95D, coordinates[0]);
        assertEquals(15D, coordinates[1]);
        assertEquals(7.25D, coordinates[2]);
        printSuccessMessage("setting maximum dimensions");
    }

    @Test
    @DisplayName("Test set minimum volume")
    @Order(4)
    void testSetMinVolume() {
        Config.setMinVolume(15.75D);
        assertEquals(15.75D, Config.getMinVolume());
        printSuccessMessage("setting minimum volume");
    }

    @Test
    @DisplayName("Test set maximum volume")
    @Order(5)
    void testSetMaxVolume() {
        Config.setMaxVolume(200.1D);
        assertEquals(200.1D, Config.getMaxVolume());
        printSuccessMessage("setting maximum volume");
    }

    @Test
    @DisplayName("Test set off-limits areas")
    @Order(6)
    void testSetOffLimitsAreas() {
        List<Cuboid> cuboids = List.of(
                new Cuboid(world, -15, -64, -15, 20, 320, 20),
                new Cuboid(world, 400, 32, 400, 460, 64, 460),
                new Cuboid(world_nether, -20, -20, -20, 20, 20, 20),
                new Cuboid(world_the_end, 45, 10, 45, 90, 30, 90)
        );

        Config.setOffLimitsAreas(cuboids);
        assertEquals(cuboids.stream().map(Cuboid::toString).toList(), Config.getOffLimitsCuboids().stream().map(Cuboid::toString).toList());
        printSuccessMessage("setting off-limits areas");
    }

    @Test
    @DisplayName("Test set max owned stores")
    @Order(7)
    void testSetMaxOwnedStores() {
        Config.setMaxOwnedStores(5);
        assertEquals(5, Config.getMaxOwnedStores());
        printSuccessMessage("setting maximum owned stores");
    }

    @Test
    @DisplayName("Test set default store for all worlds")
    @Order(8)
    void testGlobalDefaultStore() {
        Config.setDefaultStore(world, otherStore);
        Config.setDefaultStore(server.getWorld("null"), store);
        assertTrue(Config.getDefaultStore(null).isPresent());
        assertTrue(Config.getDefaultStore(world).isPresent());
        assertEquals(store, Config.getDefaultStore(null).get());
        assertEquals(store, Config.getDefaultStore(world).get());
        printSuccessMessage("setting default store for all world");
    }

    @Test
    @DisplayName("Test set default store for specific worlds")
    @Order(9)
    void testDefaultStore_PerWorld() {
        Config.setDefaultStore(world, store);
        Config.setDefaultStore(world_nether, otherStore);
        assertTrue(Config.getDefaultStore(world).isPresent());
        assertTrue(Config.getDefaultStore(world_nether).isPresent());
        assertEquals(store, Config.getDefaultStore(world).get());
        assertEquals(otherStore, Config.getDefaultStore(world_nether).get());
        printSuccessMessage("setting default store for specific world");
    }
}
