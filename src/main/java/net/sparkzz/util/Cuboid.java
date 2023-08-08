package net.sparkzz.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

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
               x1 <= playerX && playerX <= x2 &&
               y1 <= playerY && playerY <= y2 &&
               z1 <= playerZ && playerZ <= z2;
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
}
