package net.sparkzz.shops.util;

import net.sparkzz.shops.Store;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Configuration class for accessing and updating shop data
 */
public class Config extends AbstractConfig {

    private static final Logger log = Sponge.pluginManager().plugin("shops").get().logger();
    
    /**
     * Gets the list of off-limits cuboids to prevent players from creating stores within "off-limits" zones
     *
     * @return the off-limits cuboids
     */
    public static List<Cuboid> getOffLimitsCuboids() {
        List<Cuboid> cuboids = new ArrayList<>();

        try {
            List<String> offLimitsAreas = getRootNode().node("store", "off-limits").getList(String.class);

            if (offLimitsAreas == null || offLimitsAreas.isEmpty())
                return cuboids;

            for (String area : offLimitsAreas) {
                if (!(area.contains("world(") && area.contains("start(") && area.contains("end(")))
                    continue;

                area = area.replace(" ", "");

                Optional<ServerWorld> world;
                double x1, y1, z1, x2, y2, z2;
                int currIndex;

                String worldName = area.substring(currIndex = area.indexOf("world(") + 6, area.indexOf(")", currIndex));

                world = Sponge.server().worldManager().worlds().stream().filter(w -> w.properties().name().equals(worldName)).findFirst();

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

                Cuboid cuboid = new Cuboid(world.orElseThrow(() -> new SerializationException("Unable to deserialize off-limits cuboid for world: " + worldName)), x1, y1, z1, x2, y2, z2);

                cuboids.add(cuboid);
            }
        } catch (SerializationException exception) {
            log.error("Unable to load off-limits areas: " + exception.getMessage());
        } catch (NumberFormatException exception) {
            log.error(exception.getMessage());
        }

        return cuboids;
    }

    /**
     * Gets the default Store for the input world
     *
     * @param world the world to get the default Store from
     * @return the default Store for the input world
     */
    public static Optional<Store> getDefaultStore(@Nullable ServerWorld world) {
        if (!getRootNode().node("store").hasChild("default"))
            return Optional.empty();

        CommentedConfigurationNode defaults = getRootNode().node("store").node("default");

        if (defaults.hasChild("null"))
            return Store.identifyStore(defaults.node("null").getString());

        String name = (world == null) ? "null" : world.properties().name();
        String uuid = (world == null) ? "null" : world.properties().uniqueId().toString();

        if (defaults.hasChild(uuid)) {
            return Store.identifyStore(defaults.node(uuid).getString());
        } else if (defaults.hasChild(name)) {
            return Store.identifyStore(defaults.node(name).getString());
        }

        return Optional.empty();
    }

    /**
     * Adds a new off-limits cuboid area to prevent players from building in that area
     *
     * @param cuboid the cuboid area to be added
     */
    public static void addOffLimitsArea(Cuboid cuboid) {
        try {
            CommentedConfigurationNode offLimitsNode = getRootNode().node("store", "off-limits");
            List<String> offLimitsAreas = offLimitsNode.getList(String.class);

            if (offLimitsAreas == null)
                offLimitsAreas = new ArrayList<>();

            offLimitsAreas.add(String.format("world(%s),start(%f,%f,%f),end(%f,%f,%f)", cuboid.getWorld().properties().name(),
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
    public static void setDefaultStore(@Nullable ServerWorld world, @Nullable Store store) {
        try {
            CommentedConfigurationNode defaults = getRootNode().node("store", "default");

            if (store == null) {
                defaults.removeChild((world == null) ? "null" : world.properties().name());
                return;
            }

            if (world == null || world.properties().name().equalsIgnoreCase("null")) {
                defaults.node("null").set(store.getUUID().toString());
                return;
            }

            defaults.node(world.properties().name()).set(store.getUUID());
        } catch (SerializationException exception) {
            log.warn(String.format("Unable to set default store: (World: %s, Store: %s)", world, store));
        }
    }

    /**
     * Sets the limit of stores that a player can own
     *
     * @param quantity the number of stores that a player can own
     */
    public static void setMaxOwnedStores(int quantity) {
        try {
            getRootNode().node("store", "max-owned-stores").set(quantity);
        } catch (SerializationException exception) {
            log.error(exception.getMessage());
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
            getRootNode().node("store", "max-volume").set(volume);
        } catch (SerializationException exception) {
            log.error(exception.getMessage());
        }
    }

    /**
     * Sets the off-limits areas to the newly defined list of cuboids
     *
     * @param cuboids the cuboids to be set as off-limits areas
     */
    public static void setOffLimitsAreas(List<Cuboid> cuboids) {
        try {
            getRootNode().node("store").node("off-limits").setList(String.class, null);

            for (Cuboid cuboid : cuboids)
                addOffLimitsArea(cuboid);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }
}
