package net.sparkzz.shops.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.leangen.geantyref.TypeToken;
import net.sparkzz.shops.Store;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.world.server.ServerWorld;
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
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.DoubleStream;

/**
 * Helper class to manage saving and loading of Stores
 *
 * @author Brendon Butler
 */
public class Warehouse {

    protected static final Logger log = Sponge.pluginManager().plugin("shops").orElseThrow().logger();
    private static CommentedConfigurationNode config, storeConfig;
    private static ConfigurationLoader<CommentedConfigurationNode> configLoader, storeLoader;
    private static ObjectMapper<Store> storeMapper;
    private static final String configName = "config.yml";
    private static final String storeConfigName = "data.shops";

    /**
     * Loads the configuration(s)
     *
     * @return whether the configuration(s) were loaded successfully
     */
    public static boolean loadConfig() {
        TypeSerializerCollection serializers = ConfigurationOptions.defaults().serializers().childBuilder()
                .register(new TypeToken<>() {}, new ItemTypeMapSerializer())
                .register(TypeToken.get(Cuboid.class), new CuboidSerializer())
                .register(TypeToken.get(ServerWorld.class), new WorldSerializer())
                .register(TypeToken.get(BigDecimal.class), new BigDecimalSerializer())
                .build();
        ConfigurationOptions options = ConfigurationOptions.defaults().serializers(serializers);

        File dataFolder = Sponge.configManager().pluginConfig(Sponge.pluginManager().plugin("shops").orElseThrow()).directory().toFile();
        boolean dirExists = dataFolder.exists();

        if (!dirExists) dirExists = dataFolder.mkdirs();

        if (!dirExists) {
            log.error("Error loading or creating data folder");
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
                                    (configFile.exists()) ?
                                    new FileInputStream(configFile) :
                                    Objects.requireNonNull(Sponge.pluginManager().plugin("shops").orElseThrow()
                                            .openResource(new URI("config.yml")).orElseThrow())
                            )
                    )
                )
                .sink(() -> new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile)))).build();

        try {
            config = configLoader.load();
            storeConfig = storeLoader.load(options);

            if (config == null || storeConfig == null) throw new IOException();
        } catch (IOException exception) {
            log.error("Error loading config file(s), disabling Shops plugin");
            return false;
        }

        Config.setRootNode(config);
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
            log.error("Error saving configuration");
        }
    }

    /**
     * Loads the stores from the data.shops file
     */
    private static void loadStores() {
        try {
            storeMapper = ObjectMapper.factory().get(TypeToken.get(Store.class));

            for (CommentedConfigurationNode currentNode : storeConfig.node("stores").childrenList())
                Store.STORES.add(storeMapper.load(currentNode));

            Optional<Store> nullDefaultStore = Config.getDefaultStore(null);

            if (nullDefaultStore.isPresent()) {
                Store.setDefaultStore(null, nullDefaultStore.get());
            } else {
                for (ServerWorld world : Sponge.server().worldManager().worlds()) {
                    Optional<Store> defaultStoreForWorld = Config.getDefaultStore(world);

                    defaultStoreForWorld.ifPresent(store -> Store.setDefaultStore(world, store));
                }
            }

            log.info(String.format("%d %s loaded", Store.STORES.size(), (Store.STORES.size() == 1) ? "store" : "stores"));
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
            storesNode.set(new ArrayList<Store>());

            int i = 0;

            for (Store store : Store.getStores()) {
                ConfigurationNode storeNode = storesNode.node(i);
                storeMapper.save(store, storeNode);

                if (store.getCuboidLocation() != null) {
                    storeNode.node("location").set(store.getCuboidLocation());
                }

                i++;
            }

            log.info(String.format("%d %s saved", i, (Store.STORES.size() == 1) ? "store" : "stores"));
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
            ServerWorld world = node.node("world").get(TypeToken.get(ServerWorld.class));
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
    static class ItemTypeMapSerializer implements TypeSerializer<Map<ItemType, Map<String, Number>>> {

        private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        /**
         * Configures the deserializer to properly map and deserialize store item data
         *
         * @param type the provided type
         * @param node the provided base node for stores
         * @return the deserialized store item data
         */
        @Override
        public Map<ItemType, Map<String, Number>> deserialize(Type type, ConfigurationNode node) {
            try {
                String json = node.getString("items");
                return mapper.readValue(json, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize material map");
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
        public void serialize(Type type, @Nullable Map<ItemType, Map<String, Number>> obj, ConfigurationNode node) throws SerializationException {
            try {
                if (obj != null && !obj.isEmpty()) {
                    String json = mapper.writeValueAsString(obj);
                    node.set(json);
                }
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize material map");
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
        public @Nullable Map<ItemType, Map<String, Number>> emptyValue(Type specificType, ConfigurationOptions options) {
            return new HashMap<>();
        }
    }

    /**
     * Helper class to serialize and deserialized worlds
     */
    static class WorldSerializer implements TypeSerializer<ServerWorld> {

        /**
         * Configures the deserializer to properly map and deserialize store item data
         *
         * @param type the provided type
         * @param node the provided base node for stores
         * @return the deserialized store item data
         */
        @Override
        public @Nullable ServerWorld deserialize(Type type, ConfigurationNode node) {
            String worldKey = node.getString();
            ResourceKey key = ResourceKey.resolve(worldKey);
            ServerWorld world = null;

            if (!key.asString().isEmpty()) {
                try {
                    world = Sponge.server().worldManager().world(key).orElseThrow();
                } catch (NoSuchElementException exception) {
                    log.error("Unable to load store ({}) for world: {}", Objects.requireNonNull(Objects.requireNonNull(node.parent()).parent()).node("name").getString(), worldKey);
                }
            }

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
        public void serialize(Type type, @Nullable ServerWorld world, ConfigurationNode node) throws SerializationException {
            if (world != null)
                node.set(world.properties().key().toString());
        }

        /**
         * Handles empty values
         *
         * @param specificType the provided type
         * @param options the provided options
         * @return an empty map
         */
        @Override
        public @Nullable ServerWorld emptyValue(Type specificType, ConfigurationOptions options) {
            return null;
        }
    }

    /**
     * Helper class to serialize and deserialized BigDecimal values
     */
    static class BigDecimalSerializer implements TypeSerializer<BigDecimal> {

        /**
         * Configures the deserializer to properly deserialize BigDecimal values
         *
         * @param type the provided type
         * @param node the provided node
         * @return the deserialized BigDecimal value
         */
        @Override
        public @Nullable BigDecimal deserialize(Type type, ConfigurationNode node) {
            String nodeValue = node.getString();

            return (nodeValue != null && !nodeValue.isEmpty()) ? new BigDecimal(nodeValue) : BigDecimal.ZERO;
        }

        /**
         * Configures the serializer to properly serialize BigDecimal values
         *
         * @param type the provided type
         * @param bigDecimal the provided BigDecimal value
         * @param node the provided node
         */
        @Override
        public void serialize(Type type, @Nullable BigDecimal bigDecimal, ConfigurationNode node) throws SerializationException {
            node.set((bigDecimal != null) ? bigDecimal.toString() : BigDecimal.ZERO.toString());
        }

        /**
         * Handles empty values
         *
         * @param specificType the provided type
         * @param options the provided options
         * @return a zero BigDecimal value
         */
        @Override
        public @Nullable BigDecimal emptyValue(Type specificType, ConfigurationOptions options) {
            return BigDecimal.ZERO;
        }
    }
}
