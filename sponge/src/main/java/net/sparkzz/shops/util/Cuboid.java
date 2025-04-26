package net.sparkzz.shops.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.math.vector.Vector3d;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * The Cuboid class stores the starting and ending location for a store along with the world
 */
@ConfigSerializable
public class Cuboid extends AbstractCuboid {

    private ServerWorld world;

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
    public Cuboid(@Nullable final ServerWorld world, final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.world = world;
    }

    /**
     * Constructor for creating a Cuboid within the world
     *
     * @param startingLocation the starting location of the cuboid
     * @param endingLocation the ending location of the cuboid
     */
    public Cuboid(@NotNull final ServerLocation startingLocation, @NotNull final Vector3d endingLocation) {
        super(startingLocation.x(), startingLocation.y(), startingLocation.z(), endingLocation.x(), endingLocation.y(), endingLocation.z());
        this.world = startingLocation.world();
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
        ServerLocation playerLocation = (ServerLocation) player.location();

        return world == playerLocation.world() && super.isPointWithin(playerLocation.x(), playerLocation.y(), playerLocation.z());
    }

    /**
     * Gets the world that the Cuboid is contained within
     *
     * @return the world that the Cuboid is contained within
     */
    @Nullable
    public ServerWorld getWorld() {
        if (this.world == null)
            return null;

        return this.world;
    }

    /**
     * Sets the world for the store
     *
     * @param world the world to be associated with the Cuboid
     */
    public void setWorld(@NotNull ServerWorld world) {
        this.world = world;
    }

    /**
     * Determines whether this cuboid is equal to another object
     *
     * @param object the other object to compare against
     * @return whether the cuboids are equal
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null || getClass() != object.getClass())
            return false;

        Cuboid otherCuboid = (Cuboid) object;
        return this.world.equals(otherCuboid.getWorld()) &&
               super.getX1() == otherCuboid.getX1() &&
               super.getY1() == otherCuboid.getY1() &&
               super.getZ1() == otherCuboid.getZ1() &&
               super.getX2() == otherCuboid.getX2() &&
               super.getY2() == otherCuboid.getY2() &&
               super.getZ2() == otherCuboid.getZ2();
    }

    /**
     * Generates a string containing the world name and coordinate points
     *
     * @return a formatted string for the Cuboid
     */
    @Override
    public String toString() {
        return String.format("%s(%.2f, %.2f, %.2f), (%.2f, %.2f, %.2f)",
                ((world != null) ? world.properties().name() + ", " : ""),
                super.getX1(), super.getY1(), super.getZ1(), super.getX2(), super.getY2(), super.getZ2());
    }
}
