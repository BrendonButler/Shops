package net.sparkzz.shops.util;

import net.sparkzz.shops.AbstractStore;
import net.sparkzz.shops.Core;
import net.sparkzz.shops.Store;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Configuration class for accessing and updating
 */
public class Config {

    private static final Logger log = Core.getLogger();

    private static CommentedConfigurationNode rootNode;

    public static CommentedConfigurationNode getRootNode() {
        return rootNode;
    }

    private static double[] getDimensions(CommentedConfigurationNode node) {
        double[] dimensions = new double[3];

        dimensions[0] = node.node("x").getDouble();
        dimensions[1] = node.node("y").getDouble();
        dimensions[2] = node.node("z").getDouble();

        return dimensions;
    }

    private static void setDimensions(CommentedConfigurationNode node, double x, double y, double z) {
        try {
            node.node("x").set(x);
            node.node("y").set(y);
            node.node("z").set(z);
        } catch (SerializationException exception) {
            log.severe(exception.getMessage());
        }
    }

    /**
     * Gets the maximum (limit) dimensions to prevent players from creating stores greater than specified X, Y, and Z
     * coordinates
     *
     * @return the maximum dimensions
     */
    public static double[] getMaxDimensions() {
        CommentedConfigurationNode maxDimensions = rootNode.node("store", "max-dimensions");

        return getDimensions(maxDimensions);
    }

    /**
     * Gets the maximum (limit) volume to prevent players from creating stores where the product of the difference of
     * their X1, Y1, Z1, X2, Y2, and Z2 coordinates has a volume greater than said maximum
     *
     * @return the maximum volume
     */
    public static double getMaxVolume() {
        return rootNode.node("store", "max-volume").getDouble();
    }

    /**
     * Gets the minimum (limit) volume to prevent players from creating stores where the product of the difference of
     * their X1, Y1, Z1, X2, Y2, and Z2 coordinates has a volume lesser than said minimum
     *
     * @return the minimum volume
     */
    public static double getMinVolume() {
        return rootNode.node("store", "min-volume").getDouble();
    }

    /**
     * Gets the minimum (limit) dimensions to prevent players from creating stores lesser than specified X, Y, and Z
     * coordinates
     *
     * @return the minimum dimensions
     */
    public static double[] getMinDimensions() {
        CommentedConfigurationNode minDimensions = rootNode.node("store", "min-dimensions");

        return getDimensions(minDimensions);
    }

    /**
     * Sets the limit of stores that a player can own (this can be overridden by an admin creating a store and
     * transferring it to said player
     *
     * @return the maximum number of stores a player can own or create
     */
    public static int getMaxOwnedStores() {
        return rootNode.node("store", "max-owned-stores").getInt();
    }

    /**
     * Gets the list of off-limits cuboids to prevent players from creating stores within "off-limits" zones
     *
     * @return the off-limits cuboids
     */
    public static List<Cuboid> getOffLimitsCuboids() {
        List<Cuboid> cuboids = new ArrayList<>();

        try {
            List<String> offLimitsAreas = rootNode.node("store", "off-limits").getList(String.class);

            if (offLimitsAreas == null || offLimitsAreas.isEmpty())
                return cuboids;

            for (String area : offLimitsAreas) {
                if (!(area.contains("world(") && area.contains("start(") && area.contains("end(")))
                    continue;

                area = area.replace(" ", "");

                World world;
                double x1, y1, z1, x2, y2, z2;
                int currIndex;

                world = Bukkit.getWorld(area.substring(
                                currIndex = area.indexOf("world(") + 6, area.indexOf(")", currIndex)
                ));

                String[] startCoordinate = area.substring(
                        currIndex = area.indexOf("start(") + 6, area.indexOf(")", currIndex)
                ).split(",");

                String[] endCoordinate = area.substring(
                        currIndex = area.indexOf("end(") + 4, area.indexOf(")", currIndex)
                ).split(",");

                x1 = Double.parseDouble(startCoordinate[0]);
                y1 = Double.parseDouble(startCoordinate[1]);
                z1 = Double.parseDouble(startCoordinate[2]);
                x2 = Double.parseDouble(endCoordinate[0]);
                y2 = Double.parseDouble(endCoordinate[1]);
                z2 = Double.parseDouble(endCoordinate[2]);

                Cuboid cuboid = new Cuboid(world, x1, y1, z1, x2, y2, z2);

                cuboids.add(cuboid);
            }
        } catch (SerializationException exception) {
            log.severe("Unable to load off-limits areas");
        } catch (NumberFormatException exception) {
            log.severe(exception.getMessage());
        }

        return cuboids;
    }

    /**
     * Gets the default Store for the input world
     *
     * @param world the world to get the default Store from
     * @return the default Store for the input world
     */
    public static Optional<AbstractStore> getDefaultStore(@Nullable World world) {
        if (!rootNode.node("store").hasChild("default"))
            return Optional.empty();

        CommentedConfigurationNode defaults = rootNode.node("store").node("default");

        if (defaults.hasChild("null"))
            return Store.identifyStore(defaults.node("null").getString());

        String name = (world == null) ? "null" : world.getName();
        String uuid = (world == null) ? "null" : world.getUID().toString();

        if (defaults.hasChild(uuid)) {
            return Store.identifyStore(defaults.node(uuid).getString());
        } else if (defaults.hasChild(name)) {
            return Store.identifyStore(defaults.node(name).getString());
        }

        return Optional.empty();
    }

    /**
     * Gets the custom response message to be sent to the player in place of the defaults
     *
     * @param key the CipherKey to be used as the map key
     * @return the custom response message
     */
    public static String getMessage(Notifier.CipherKey key) {
        return rootNode.node("messages").node(key.name()).getString();
    }

    /**
     * Adds a new off-limits cuboid area to prevent players from building in that area
     *
     * @param cuboid the cuboid area to be added
     */
    public static void addOffLimitsArea(Cuboid cuboid) {
        try {
            CommentedConfigurationNode offLimitsNode = rootNode.node("store", "off-limits");
            List<String> offLimitsAreas = offLimitsNode.getList(String.class);

            if (offLimitsAreas == null)
                offLimitsAreas = new ArrayList<>();

            offLimitsAreas.add(String.format("world(%s),start(%f,%f,%f),end(%f,%f,%f)", cuboid.getWorld().getName(),
                    cuboid.getX1(), cuboid.getY1(), cuboid.getZ1(), cuboid.getX2(), cuboid.getY2(), cuboid.getZ2()));

            offLimitsNode.setList(String.class, offLimitsAreas);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the default Store for a selected world
     *
     * @param world the world to have the Store associated with
     * @param store the Store to be associated with the input world
     */
    public static void setDefaultStore(@Nullable World world, @Nullable Store store) {
        try {
            CommentedConfigurationNode defaults = rootNode.node("store", "default");

            if (store == null) {
                defaults.removeChild((world == null) ? "null" : world.getName());
                return;
            }

            if (world == null || world.getName().equalsIgnoreCase("null")) {
                defaults.node("null").set(store.getUUID().toString());
                return;
            }

            defaults.node(world.getName()).set(store.getUUID());
        } catch (SerializationException exception) {
            log.warning(String.format("Unable to set default store: (World: %s, Store: %s)", world, store));
        }
    }

    /**
     * Sets the maximum dimensions
     *
     * @param x the X coordinate to be set
     * @param y the Y coordinate to be set
     * @param z the Z coordinate to be set
     */
    public static void setMaxDimensions(double x, double y, double z) {
        CommentedConfigurationNode maxDimensionsNode = rootNode.node("store", "max-dimensions");

        setDimensions(maxDimensionsNode, x, y, z);
    }

    /**
     * Sets the limit of stores that a player can own
     *
     * @param quantity the number of stores that a player can own
     */
    public static void setMaxOwnedStores(int quantity) {
        try {
            rootNode.node("store", "max-owned-stores").set(quantity);
        } catch (SerializationException exception) {
            log.severe(exception.getMessage());
        }
    }

    /**
     * Sets the maximum volume that a store can be based on the product of the difference of its X1, Y1, Z1, X2, Y2, and
     * Z2 coordinates
     *
     * @param volume the maximum volume to be set
     */
    public static void setMaxVolume(double volume) {
        try {
            rootNode.node("store", "max-volume").set(volume);
        } catch (SerializationException exception) {
            log.severe(exception.getMessage());
        }
    }

    /**
     * Sets the minimum dimensions
     *
     * @param x the X coordinate to be set
     * @param y the Y coordinate to be set
     * @param z the Z coordinate to be set
     */
    public static void setMinDimensions(double x, double y, double z) {
        CommentedConfigurationNode minDimensionsNode = rootNode.node("store", "min-dimensions");

        setDimensions(minDimensionsNode, x, y, z);
    }

    /**
     * Sets the minimum volume that a store can be based on the product of the difference of its X1, Y1, Z1, X2, Y2, and
     * Z2 coordinates
     *
     * @param volume the minimum volume to be set
     */
    public static void setMinVolume(double volume) {
        try {
            rootNode.node("store", "min-volume").set(volume);
        } catch (SerializationException exception) {
            log.severe(exception.getMessage());
        }
    }

    /**
     * Sets the off-limits areas to the newly defined list of cuboids
     *
     * @param cuboids the cuboids to be set as off-limits areas
     */
    public static void setOffLimitsAreas(List<Cuboid> cuboids) {
        try {
            rootNode.node("store").node("off-limits").setList(String.class, null);

            for (Cuboid cuboid : cuboids)
                addOffLimitsArea(cuboid);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the root node of the configuration
     *
     * @param node the root node of the configuration
     */
    public static void setRootNode(CommentedConfigurationNode node) {
        rootNode = node;
    }
}
