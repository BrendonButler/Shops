package net.sparkzz.util;

import net.sparkzz.shops.Shops;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Configuration class for accessing and updating
 */
public class Config {

    private static final Logger log = Shops.getPlugin(Shops.class).getLogger();

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

    public static double[] getMaxDimensions() {
        CommentedConfigurationNode maxDimensions = rootNode.node("store", "max-dimensions");

        return getDimensions(maxDimensions);
    }

    public static double getMaxVolume() {
        return rootNode.node("store", "max-volume").getDouble();
    }

    public static double getMinVolume() {
        return rootNode.node("store", "min-volume").getDouble();
    }

    public static double[] getMinDimensions() {
        CommentedConfigurationNode minDimensions = rootNode.node("store", "min-dimensions");

        return getDimensions(minDimensions);
    }

    public static int getMaxOwnedStores() {
        return rootNode.node("store", "max-owned-stores").getInt();
    }

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

    public static String getMessage(Notifier.CipherKey key) {
        return rootNode.node("messages").node(key.name()).getString();
    }

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

    public static void setMaxDimensions(double x, double y, double z) {
        CommentedConfigurationNode maxDimensionsNode = rootNode.node("store", "max-dimensions");

        setDimensions(maxDimensionsNode, x, y, z);
    }

    public static void setMaxOwnedStores(int quantity) {
        try {
            rootNode.node("store", "max-owned-stores").set(quantity);
        } catch (SerializationException exception) {
            log.severe(exception.getMessage());
        }
    }

    public static void setMaxVolume(double volume) {
        try {
            rootNode.node("store", "max-volume").set(volume);
        } catch (SerializationException exception) {
            log.severe(exception.getMessage());
        }
    }

    public static void setMinDimensions(double x, double y, double z) {
        CommentedConfigurationNode minDimensionsNode = rootNode.node("store", "min-dimensions");

        setDimensions(minDimensionsNode, x, y, z);
    }

    public static void setMinVolume(double volume) {
        try {
            rootNode.node("store", "min-volume").set(volume);
        } catch (SerializationException exception) {
            log.severe(exception.getMessage());
        }
    }

    public static void setRootNode(CommentedConfigurationNode node) {
        rootNode = node;
    }
}
