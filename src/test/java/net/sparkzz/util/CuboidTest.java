package net.sparkzz.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static net.sparkzz.shops.TestHelper.printMessage;
import static net.sparkzz.shops.TestHelper.printSuccessMessage;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cuboid Test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CuboidTest {

    private static Cuboid cuboid, noWorldCuboid;
    private static ServerMock server;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST CUBOID ]==");
        server = MockBukkit.getOrCreateMock();
    }

    @BeforeEach
    void setUpCuboids() {
        cuboid = new Cuboid(server.getWorld("world"), -20D, -20D, -20D, 20D, 20D, 20D);
        noWorldCuboid = new Cuboid(null, -60D, -60D, -60D, 19D, 19D, 19D);
    }

    @Test
    @DisplayName("Test Cuboid - get world (\"world\")")
    @Order(1)
    void testGetWorld_World() {
        assertEquals(server.getWorld("world"), cuboid.getWorld());
        printSuccessMessage("Cuboid - get world (\"world\")");
    }

    @Test
    @DisplayName("Test Cuboid - get world (missing world)")
    @Order(2)
    void testGetWorld_NullWorld() {
        assertNull(noWorldCuboid.getWorld());
        printSuccessMessage("Cuboid - get world (missing world)");
    }

    @Test
    @DisplayName("Test Cuboid - get coordinates")
    @Order(3)
    void testGetCoordinates() {
        assertEquals(-20D, cuboid.getX1());
        assertEquals(-20D, cuboid.getY1());
        assertEquals(-20D, cuboid.getZ1());
        assertEquals(20D, cuboid.getX2());
        assertEquals(20D, cuboid.getY2());
        assertEquals(20D, cuboid.getZ2());
        printSuccessMessage("Cuboid - get coordinates");
    }

    @Test
    @DisplayName("Test Cuboid - update starting location")
    @Order(4)
    void testUpdateCoordinates_StartingLocation() {
        cuboid.updateStartLocation(-48D, -49D, -50D);

        assertEquals(-48D, cuboid.getX1());
        assertEquals(-49D, cuboid.getY1());
        assertEquals(-50D, cuboid.getZ1());
        assertEquals(20D, cuboid.getX2());
        assertEquals(20D, cuboid.getY2());
        assertEquals(20D, cuboid.getZ2());
        printSuccessMessage("Cuboid - update starting location");
    }

    @Test
    @DisplayName("Test Cuboid - update ending location")
    @Order(5)
    void testUpdateCoordinates_EndingLocation() {
        cuboid.updateEndingLocation(98D, 99D, 100D);

        assertEquals(-20D, cuboid.getX1());
        assertEquals(-20D, cuboid.getY1());
        assertEquals(-20D, cuboid.getZ1());
        assertEquals(98D, cuboid.getX2());
        assertEquals(99D, cuboid.getY2());
        assertEquals(100D, cuboid.getZ2());
        printSuccessMessage("Cuboid - update ending location");
    }

    @Test
    @DisplayName("Test Cuboid - update world")
    @Order(6)
    void testUpdateWorld() {
        World world = server.createWorld(new WorldCreator("other-world"));
        cuboid.setWorld(world);

        assertEquals(server.getWorld("other-world"), cuboid.getWorld());
        printSuccessMessage("Cuboid - update world");
    }

    @Nested
    @DisplayName("Test Cuboid Bounds")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class TestCuboidBounds {

        private static PlayerMock mrSparkzz;

        @BeforeAll
        static void setUp() {
            mrSparkzz = server.addPlayer("MrSparkzz");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - outside X (low)")
        @Order(1)
        void testCuboidBounds_LowX() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), -21D, -20D, -20D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - outside X (low)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - outside X (high)")
        @Order(2)
        void testCuboidBounds_HighX() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), 21D, -20D, -20D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - outside X (high)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - outside Y (low)")
        @Order(3)
        void testCuboidBounds_LowY() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), -20D, -21D, -20D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - outside Y (low)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - outside Y (high)")
        @Order(4)
        void testCuboidBounds_HighY() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), -20D, 21D, -20D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - outside Y (high)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - outside Z (low)")
        @Order(5)
        void testCuboidBounds_LowZ() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), -20D, -20D, -21D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - outside Z (low)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - outside Z (high)")
        @Order(6)
        void testCuboidBounds_HighZ() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), -20D, -20D, 21D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - outside Z (high)");
        }
    }
}
