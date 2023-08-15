package net.sparkzz.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The Cuboid class stores the starting and ending location for a store along with the world
 */
@ConfigSerializable
public class Cuboid {

    private World world;
    private double x1, y1, z1;
    private double x2, y2, z2;

    /**
     * This constructor is required for the deserializer
     *
     * @deprecated Do not use this constructor!
     */
    @Deprecated
    public Cuboid() {}

    /**
     * Constructor for creating a Cuboid within the world
     *
     * @param world the world the cuboid is located within
     * @param x1 the starting 'x' position in the world
     * @param y1 the starting 'y' position in the world
     * @param z1 the starting 'z' position in the world
     * @param x2 the ending 'x' position in the world
     * @param y2 the ending 'y' position in the world
     * @param z2 the ending 'z' position in the world
     */
    public Cuboid(@Nullable final World world, final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        this.world = world;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    /**
     * determines all points (as integers) between two coordinates on a plane, this plane can be adjusted by the
     * respective third coordinate
     * (ex: XY would use Z1 for the front face, and Z2 for the back face)
     * <br>
     * face 1 is the XY face (vertical front/rear) (adjust by Z1 and Z2)
     * face 2 is the ZY face (vertical sides) (adjust by X1 and X2)
     * face 3 is the XZ face (horizontal) (adjust by Y1 and Y2)
     *
     * @return A list of coordinates that create a point on a 2D plane
     */
    private List<List<Point2D>> getFacePoints() {
        List<List<Point2D>> facePoints = new LinkedList<>();
        facePoints.add(0, new ArrayList<>());
        facePoints.add(1, new ArrayList<>());
        facePoints.add(2, new ArrayList<>());

        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        double minZ = Math.min(z1, z2);
        double maxZ = Math.max(z1, z2);

        // loop through 3 faces
        for (int i = 0; i <= 2; i++) {
            int min, max, min2, max2;

            switch (i) {
                case 0 -> {
                    min = (int) Math.floor(minX);
                    max = (int) Math.ceil(maxX);
                    min2 = (int) Math.floor(minY);
                    max2 = (int) Math.ceil(maxY);
                }
                case 1 -> {
                    min = (int) Math.floor(minZ);
                    max = (int) Math.ceil(maxZ);
                    min2 = (int) Math.floor(minY);
                    max2 = (int) Math.ceil(maxY);
                }
                default -> {
                    min = (int) Math.floor(minX);
                    max = (int) Math.ceil(maxX);
                    min2 = (int) Math.floor(minZ);
                    max2 = (int) Math.ceil(maxZ);
                }
            }

            for (double x = min; x <= max; x++) {
                for (double y = min2; y <= max2; y++) {
                    facePoints.get(i).add(new Point2D.Double(x, y));
                }
            }
        }

        return facePoints;
    }

    /**
     * Checks whether the current cuboid intersects another cuboid
     *
     * @param cuboid the other cuboid to check intersections against
     * @return whether the current cuboid intersects another cuboid
     */
    public boolean intersects(Cuboid cuboid) {
        boolean intersects = false;

        if (cuboid == null || world == null || cuboid.getWorld() == null || !world.equals(cuboid.getWorld()))
            return intersects;

        if (this.equals(cuboid))
            return true;

        List<List<Point2D>> faces = cuboid.getFacePoints();

        for (int i = 0; i <= 2; i++) {
            List<Point2D> facePoints = faces.get(i);

            switch (i) {
                case 0 -> {
                    int minZ = (int) Math.floor(Math.min(cuboid.getZ1(), cuboid.getZ2()));
                    int maxZ = (int) Math.floor(Math.max(cuboid.getZ1(), cuboid.getZ2()));

                    for (Point2D point : facePoints) {
                        if (isPointWithin(point.getX(), point.getY(), minZ))
                            return true;
                        if (isPointWithin(point.getX(), point.getY(), maxZ))
                            return true;
                    }
                }
                case 1 -> {
                    int minX = (int) Math.floor(Math.min(cuboid.getX1(), cuboid.getX2()));
                    int maxX = (int) Math.floor(Math.max(cuboid.getX1(), cuboid.getX2()));

                    for (Point2D point : facePoints) {
                        if (isPointWithin(minX, point.getY(), point.getX()))
                            return true;
                        if (isPointWithin(maxX, point.getY(), point.getX()))
                            return true;
                    }
                }
                default -> {
                    int minY = (int) Math.floor(Math.min(cuboid.getY1(), cuboid.getY2()));
                    int maxY = (int) Math.floor(Math.max(cuboid.getY1(), cuboid.getY2()));

                    for (Point2D point : facePoints) {
                        if (isPointWithin(point.getX(), minY, point.getY()))
                            return true;
                        if (isPointWithin(point.getX(), maxY, point.getY()))
                            return true;
                    }
                }
            }
        }

        return intersects;
    }

    /**
     * Determines whether a player is within the bounds of the cuboid
     *
     * @param player the player to be checked
     * @return whether the player is within the bounds of the cuboid
     */
    public boolean isPlayerWithin(Player player) {
        Location playerLocation = player.getLocation();
        double playerX = playerLocation.getX();
        double playerY = playerLocation.getY();
        double playerZ = playerLocation.getZ();

        return world == player.getWorld() &&
               this.x1 <= playerX && playerX <= this.x2 &&
               this.y1 <= playerY && playerY <= this.y2 &&
               this.z1 <= playerZ && playerZ <= this.z2;
    }

    /**
     * Determines whether a point is within the bounds of the cuboid
     *
     * @param x the x coordinate to check
     * @param y the y coordinate to check
     * @param z the z coordinate to check
     * @return whether the point is within the bounds of the cuboid
     */
    public boolean isPointWithin(double x, double y, double z) {
        return this.x1 < x && x < this.x2 &&
               this.y1 < y && y < this.y2 &&
               this.z1 < z && z < this.z2;
    }

    /**
     * Gets the X1 coordinate
     *
     * @return the value of x1
     */
    public double getX1() {
        return x1;
    }

    /**
     * Gets the X2 coordinate
     *
     * @return the value of x2
     */
    public double getX2() {
        return x2;
    }

    /**
     * Gets the Y1 coordinate
     *
     * @return the value of y1
     */
    public double getY1() {
        return y1;
    }

    /**
     * Gets the Y2 coordinate
     *
     * @return the value of y2
     */
    public double getY2() {
        return y2;
    }

    /**
     * Gets the Z1 coordinate
     *
     * @return the value of z1
     */
    public double getZ1() {
        return z1;
    }

    /**
     * Gets the Z2 coordinate
     *
     * @return the value of z2
     */
    public double getZ2() {
        return z2;
    }

    /**
     * Gets the world that the Cuboid is contained within
     *
     * @return the world that the Cuboid is contained within
     */
    @Nullable
    public World getWorld() {
        if (this.world == null)
            return null;

        return this.world;
    }

    /**
     * Sets the world for the store
     *
     * @param world the world to be associated with the Cuboid
     */
    public void setWorld(@NotNull World world) {
        this.world = world;
    }

    /**
     * Updates the starting location for the cuboid
     *
     * @param x the starting 'x' coordinate
     * @param y the starting 'y' coordinate
     * @param z the starting 'z' coordinate
     */
    public void updateStartLocation(double x, double y, double z) {
        this.x1 = x;
        this.y1 = y;
        this.z1 = z;
    }

    /**
     * Updates the ending location for the cuboid
     *
     * @param x the ending 'x' coordinate
     * @param y the ending 'y' coordinate
     * @param z the ending 'z' coordinate
     */
    public void updateEndingLocation(double x, double y, double z) {
        this.x2 = x;
        this.y2 = y;
        this.z2 = z;
    }

    /**
     * Generates a string containing the world name and coordinate points
     *
     * @return a formatted string for the Cuboid
     */
    @Override
    public String toString() {
        return String.format("%s(%.2f, %.2f, %.2f), (%.2f, %.2f, %.2f)", ((world != null) ? world.getName() + ", " : ""), x1, y1, z1, x2, y2, z2);
    }
}
