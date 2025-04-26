package net.sparkzz.shops.util;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.logging.Logger;

/**
 * Configuration class for accessing and updating shop data
 */
public class AbstractConfig {

    private static final Logger log = Logger.getLogger("Shops");

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
     * Gets the custom response message to be sent to the player in place of the defaults
     *
     * @param key the CipherKey to be used as the map key
     * @return the custom response message
     */
    public static String getMessage(AbstractNotifier.CipherKey key) {
        return rootNode.node("messages").node(key.name()).getString();
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
     * Sets the root node of the configuration
     *
     * @param node the root node of the configuration
     */
    public static void setRootNode(CommentedConfigurationNode node) {
        rootNode = node;
    }
}
