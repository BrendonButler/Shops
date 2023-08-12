package net.sparkzz.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.leangen.geantyref.TypeToken;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;

import static net.sparkzz.shops.Store.STORES;

/**
 * Helper class to manage saving and loading of Stores
 *
 * @author Brendon Butler
 */
public class Warehouse {

    private static CommentedConfigurationNode config, storeConfig;
    private static ConfigurationLoader<CommentedConfigurationNode> configLoader, storeLoader;
    private static ObjectMapper<Store> storeMapper;
    private static final Logger log = Shops.getPlugin(Shops.class).getLogger();
    private static final String configName = "config.yml";
    private static final String storeConfigName = "data.shops";

    /**
     * Loads the configuration(s)
     *
     * @param shops the Shops plugin instance to be loaded
     * @return whether the configuration(s) were loaded successfully
     */
    public static boolean loadConfig(Shops shops) {
        TypeSerializerCollection serializers = ConfigurationOptions.defaults().serializers().childBuilder().register(
                new TypeToken<>() {},
                new MaterialMapSerializer()).register(TypeToken.get(Cuboid.class),
                new CuboidSerializer()).register(TypeToken.get(World.class), new WorldSerializer()
        ).build();
        ConfigurationOptions options = ConfigurationOptions.defaults().serializers(serializers);

        File dataFolder = shops.getDataFolder();
        boolean dirExists = dataFolder.exists();

        if (!dirExists) dirExists = dataFolder.mkdirs();

        if (!dirExists) {
            log.severe("Error loading or creating data folder");
            return false;
        }

        File configFile = new File(dataFolder, configName);
        File storeConfigFile = new File(dataFolder, storeConfigName);
        storeLoader = HoconConfigurationLoader.builder().file(storeConfigFile).build();
        configLoader = YamlConfigurationLoader.builder()
                .file(configFile)
                .nodeStyle(NodeStyle.BLOCK).indent(2)
                .source(() ->
                    new BufferedReader(
                            new InputStreamReader(
                                    (configFile.exists()) ? new FileInputStream(configFile) : Objects.requireNonNull(shops.getResource("config.yml"))
                            )
                    )
                )
                .sink(() -> new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile)))).build();

        try {
            config = configLoader.load();
            storeConfig = storeLoader.load(options);

            if (config == null || storeConfig == null) throw new IOException();
        } catch (IOException exception) {
            log.severe("Error loading config file(s), disabling Shops plugin");
            return false;
        }

        Config.setRootNode(config);
        Config.addOffLimitsArea(new Cuboid(Bukkit.getWorld("world"), 1, 2, 3, 4, 5, 6));
        loadStores();
        log.info("Configurations loaded successfully");

        return true;
    }

    /**
     * Saves the config to data.shops in the plugin data folder
     */
    public static void saveConfig() {
        try {
            saveStores();
            configLoader.save(config);
            storeLoader.save(storeConfig);
            log.info("Config saved successfully");
        } catch (IOException exception) {
            log.severe("Error saving configuration");
        }
    }

    /**
     * Loads the stores from the data.shops file
     */
    private static void loadStores() {
        try {
            storeMapper = ObjectMapper.factory().get(TypeToken.get(Store.class));

            for (CommentedConfigurationNode currentNode : storeConfig.node("stores").childrenList())
                STORES.add(storeMapper.load(currentNode));

            log.info(String.format("%d %s loaded", STORES.size(), (STORES.size() == 1) ? "shop" : "shops"));
            if (!STORES.isEmpty())
                Store.setDefaultStore(STORES.get(0)); // TODO: remove once stores are dynamically loaded
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the stores to the data.stores file
     */
    private static void saveStores() {
        try {
            CommentedConfigurationNode storesNode = storeConfig.node("stores");

            // Clear the existing stores before saving the updated list
            storesNode.childrenMap().keySet().forEach(storesNode::removeChild);

            int i = 0;

            for (Store store : STORES) {
                ConfigurationNode storeNode = storesNode.node(i);
                storeMapper.save(store, storeNode);

                if (store.getCuboidLocation() != null) {
                    storeNode.node("location").set(store.getCuboidLocation());
                }

                i++;
            }

            log.info(String.format("%d %s saved", i, (STORES.size() == 1) ? "store" : "stores"));
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper class to map materials based on their attributes and configure serialization/deserialization
     */
    static class CuboidSerializer implements TypeSerializer<Cuboid> {
        /**
         * Configures the deserializer to properly deserialize cuboids
         *
         * @param type the provided type
         * @param node the provided base node for stores
         * @return the deserialized store item data
         */
        @Override
        public @Nullable Cuboid deserialize(Type type, ConfigurationNode node) throws SerializationException {
            World world = node.node("world").get(TypeToken.get(World.class));
            double x1 = node.node("x1").getDouble();
            double x2 = node.node("x2").getDouble();
            double y1 = node.node("y1").getDouble();
            double y2 = node.node("y2").getDouble();
            double z1 = node.node("z1").getDouble();
            double z2 = node.node("z2").getDouble();

            boolean allCoordinatesEqual = DoubleStream.of(x1, x2, y1, y2, z1, z2).allMatch(value -> value == 0D);

            if (!allCoordinatesEqual)
                return new Cuboid(world, x1, y1, z1, x2, y2, z2);

            return null;
        }

        /**
         * Configures the serializer to properly serialize cuboids
         *
         * @param type the provided type
         * @param cuboid the provided cuboid
         * @param node the provided base node for stores
         */
        @Override
        public void serialize(Type type, @Nullable Cuboid cuboid, ConfigurationNode node) throws SerializationException {
            if (cuboid != null) {
                // if all coordinates are 0, return
                if (DoubleStream.of(cuboid.getX1(), cuboid.getX2(), cuboid.getY1(), cuboid.getY2(), cuboid.getZ1(), cuboid.getZ2()).allMatch(value -> value == 0D))
                    return;

                if (cuboid.getWorld() != null) node.node("world").set(cuboid.getWorld());
                node.node("x1").set(cuboid.getX1());
                node.node("y1").set(cuboid.getY1());
                node.node("z1").set(cuboid.getZ1());
                node.node("x2").set(cuboid.getX2());
                node.node("y2").set(cuboid.getY2());
                node.node("z2").set(cuboid.getZ2());
            }
        }

        /**
         * Handles empty values
         *
         * @param specificType the provided type
         * @param options      the provided options
         * @return an empty map
         */
        @Override
        public @Nullable Cuboid emptyValue(Type specificType, ConfigurationOptions options) {
            return null;
        }
    }

    /**
     * Helper class to map materials based on their attributes and configure serialization/deserialization
     */
    static class MaterialMapSerializer implements TypeSerializer<Map<Material, Map<String, Number>>> {

        private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        /**
         * Configures the deserializer to properly map and deserialize store item data
         *
         * @param type the provided type
         * @param node the provided base node for stores
         * @return the deserialized store item data
         */
        @Override
        public Map<Material, Map<String, Number>> deserialize(Type type, ConfigurationNode node) {
            try {
                String json = node.getString("items");
                return mapper.readValue(json, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.severe("Failed to deserialize material map");
            }
            return new HashMap<>();
        }

        /**
         * Configures the serializer to properly serialize store item data
         *
         * @param type the provided type
         * @param obj the provided material to attribute map
         * @param node the provided base node for stores
         */
        @Override
        public void serialize(Type type, @Nullable Map<Material, Map<String, Number>> obj, ConfigurationNode node) throws SerializationException {
            try {
                if (obj != null && !obj.isEmpty()) {
                    String json = mapper.writeValueAsString(obj);
                    node.set(json);
                }
            } catch (JsonProcessingException e) {
                log.severe("Failed to serialize material map");
            }
        }

        /**
         * Handles empty values
         *
         * @param specificType the provided type
         * @param options the provided options
         * @return an empty map
         */
        @Override
        public @Nullable Map<Material, Map<String, Number>> emptyValue(Type specificType, ConfigurationOptions options) {
            return new HashMap<>();
        }
    }

    /**
     * Helper class to serialize and deserialized worlds
     */
    static class WorldSerializer implements TypeSerializer<World> {

        /**
         * Configures the deserializer to properly map and deserialize store item data
         *
         * @param type the provided type
         * @param node the provided base node for stores
         * @return the deserialized store item data
         */
        @Override
        public @Nullable World deserialize(Type type, ConfigurationNode node) {
            String worldString = node.node("location").getString("world");
            World world = null;

            if (worldString != null && !worldString.isEmpty())
                world = Bukkit.getWorld(worldString);

            return world;
        }

        /**
         * Configures the deserializer to properly serialize store item data
         *
         * @param type the provided type
         * @param world the provided world
         * @param node the provided base node for stores
         */
        @Override
        public void serialize(Type type, @Nullable World world, ConfigurationNode node) throws SerializationException {
            if (world != null)
                node.set(world.getName());
        }

        /**
         * Handles empty values
         *
         * @param specificType the provided type
         * @param options the provided options
         * @return an empty map
         */
        @Override
        public @Nullable World emptyValue(Type specificType, ConfigurationOptions options) {
            return null;
        }
    }
}
