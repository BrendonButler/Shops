package net.sparkzz.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Store;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.junit.jupiter.api.AfterAll;
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
        server.createWorld(WorldCreator.name("world"));
    }

    @BeforeEach
    void setUpCuboids() {
        cuboid = new Cuboid(server.getWorld("world"), -20D, -20D, -20D, 20D, 20D, 20D);
        noWorldCuboid = new Cuboid(null, -60D, -60D, -60D, 19D, 19D, 19D);
    }

    @AfterAll
    static void tearDown() {
        MockBukkit.unmock();
        Store.setDefaultStore(null);
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

    @Test
    @DisplayName("Test Cuboid - intersection (no/wrong world associated with one or more cuboid)")
    @Order(7)
    void testIntersection_Fail_NoWorld() {
        boolean intersects = cuboid.intersects(noWorldCuboid);

        assertFalse(intersects);
        printSuccessMessage("Cuboid - intersection (no/wrong world)");
    }

    @Test
    @DisplayName("Test Cuboid - intersection (no/wrong world associated with one or more cuboid)")
    @Order(8)
    void testIntersection_Fail_NoWorldSelf() {
        boolean intersects = noWorldCuboid.intersects(cuboid);

        assertFalse(intersects);
        printSuccessMessage("Cuboid - intersection (no/wrong world (self))");
    }

    @Test
    @DisplayName("Test Cuboid - intersection null cuboid")
    @Order(10)
    void testIntersection_Fail_Null() {
        boolean intersects = cuboid.intersects(null);

        assertFalse(intersects);
        printSuccessMessage("Cuboid - intersection null cuboid");
    }

    @Test
    @DisplayName("Test Cuboid - intersection (self)")
    @Order(11)
    void testIntersection_Self() {
        boolean intersects = cuboid.intersects(cuboid);

        assertTrue(intersects);
        printSuccessMessage("Cuboid - intersection (self)");
    }

    @Test
    @DisplayName("Test Cuboid - intersection (Face XY Min)")
    @Order(12)
    void testIntersection_FaceXYMin() {
        Cuboid intersector = new Cuboid(server.getWorld("world"), -30D, -30D,  10D, 30D, 30D, 30D);
        boolean intersects = cuboid.intersects(intersector);

        assertTrue(intersects);
        printSuccessMessage("Cuboid - intersection");
    }

    @Test
    @DisplayName("Test Cuboid - intersection (Face XY Max)")
    @Order(13)
    void testIntersection_FaceXYMax() {
        Cuboid intersector = new Cuboid(server.getWorld("world"), -30D, -30D, -30D, 30D, 30D, 10D);
        boolean intersects = cuboid.intersects(intersector);

        assertTrue(intersects);
        printSuccessMessage("Cuboid - intersection");
    }

    @Test
    @DisplayName("Test Cuboid - intersection (Face ZY Min)")
    @Order(14)
    void testIntersection_FaceZYMin() {
        Cuboid intersector = new Cuboid(server.getWorld("world"), 10D, -30D, -30D, 30D, 30D, 30D);
        boolean intersects = cuboid.intersects(intersector);

        assertTrue(intersects);
        printSuccessMessage("Cuboid - intersection");
    }

    @Test
    @DisplayName("Test Cuboid - intersection (Face ZY Max)")
    @Order(15)
    void testIntersection_FaceZYMax() {
        Cuboid intersector = new Cuboid(server.getWorld("world"), -30D, -30D, -30D, 10D, 30D, 30D);
        boolean intersects = cuboid.intersects(intersector);

        assertTrue(intersects);
        printSuccessMessage("Cuboid - intersection");
    }

    @Test
    @DisplayName("Test Cuboid - intersection (Face XZ Min)")
    @Order(16)
    void testIntersection_FaceXZBottom() {
        Cuboid intersector = new Cuboid(server.getWorld("world"), -30D, 10D, -30D, 30D, 30D, 30D);
        boolean intersects = cuboid.intersects(intersector);

        assertTrue(intersects);
        printSuccessMessage("Cuboid - intersection");
    }

    @Test
    @DisplayName("Test Cuboid - intersection (Face XZ Max)")
    @Order(17)
    void testIntersection_FaceXZTop() {
        Cuboid intersector = new Cuboid(server.getWorld("world"), -30D, -30D, -30D, 30D, 10D, 30D);
        boolean intersects = cuboid.intersects(intersector);

        assertTrue(intersects);
        printSuccessMessage("Cuboid - intersection");
    }

    @Test
    @DisplayName("Test Cuboid - intersection (No Intersection)")
    @Order(17)
    void testIntersection_NoIntersection() {
        Cuboid intersector = new Cuboid(server.getWorld("world"), -50D, -50D, -50D, 30D, 30D, 30D);
        boolean intersects = cuboid.intersects(intersector);

        assertFalse(intersects);
        printSuccessMessage("Cuboid - intersection (no intersection)");
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
        @DisplayName("Test Cuboid Bounds - player outside X (low)")
        @Order(1)
        void testCuboidBounds_Player_LowX() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), -21D, -20D, -20D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - player outside X (low)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - player outside X (high)")
        @Order(2)
        void testCuboidBounds_Player_HighX() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), 21D, -20D, -20D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - player outside X (high)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - player outside Y (low)")
        @Order(3)
        void testCuboidBounds_Player_LowY() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), -20D, -21D, -20D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - player outside Y (low)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - player outside Y (high)")
        @Order(4)
        void testCuboidBounds_Player_HighY() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), -20D, 21D, -20D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - player outside Y (high)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - player outside Z (low)")
        @Order(5)
        void testCuboidBounds_Player_LowZ() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), -20D, -20D, -21D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - player outside Z (low)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - player outside Z (high)")
        @Order(6)
        void testCuboidBounds_Player_HighZ() {
            mrSparkzz.setLocation(new Location(server.getWorld("world"), -20D, -20D, 21D));

            assertFalse(cuboid.isPlayerWithin(mrSparkzz));
            printSuccessMessage("Cuboid Bounds - player outside Z (high)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - point outside X (low)")
        @Order(10)
        void testCuboidBounds_Point_LowX() {
            assertFalse(cuboid.isPointWithin(-21D, -19D, -19D));
            printSuccessMessage("Cuboid Bounds - point outside X (low)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - point outside X (high)")
        @Order(11)
        void testCuboidBounds_Point_HighX() {
            assertFalse(cuboid.isPointWithin(21D, -19D, -19D));
            printSuccessMessage("Cuboid Bounds - point outside X (high)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - point outside Y (low)")
        @Order(12)
        void testCuboidBounds_Point_LowY() {
            assertFalse(cuboid.isPointWithin(-19D, -21D, -19D));
            printSuccessMessage("Cuboid Bounds - point outside Y (low)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - point outside Y (high)")
        @Order(13)
        void testCuboidBounds_Point_HighY() {
            assertFalse(cuboid.isPointWithin(-19D, 21D, -19D));
            printSuccessMessage("Cuboid Bounds - point outside Y (high)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - point outside Z (low)")
        @Order(14)
        void testCuboidBounds_Point_LowZ() {
            assertFalse(cuboid.isPointWithin(-19D, -19D, -21D));
            printSuccessMessage("Cuboid Bounds - point outside Z (low)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - point outside Z (high)")
        @Order(15)
        void testCuboidBounds_Point_HighZ() {
            assertFalse(cuboid.isPointWithin(-20D, -20D, 21D));
            printSuccessMessage("Cuboid Bounds - point outside Z (high)");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - point inside X, Y, Z")
        @Order(16)
        void testCuboidBounds_Point_Within() {
            assertTrue(cuboid.isPointWithin(0D, 0D, 0D));
            printSuccessMessage("Cuboid Bounds - point inside X, Y, Z");
        }

        @Test
        @DisplayName("Test Cuboid Bounds - point outside X, Y, Z")
        @Order(17)
        void testCuboidBounds_Point_Outside() {
            assertFalse(cuboid.isPointWithin(-100D, -100D, -100D));
            printSuccessMessage("Cuboid Bounds - point outside X, Y, Z");
        }
    }
}
