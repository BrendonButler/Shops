package net.sparkzz.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.leangen.geantyref.TypeToken;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import org.bukkit.Material;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Helper class to manage saving and loading of Shops
 *
 * @author Brendon Butler
 */
public class Warehouse {

    private static CommentedConfigurationNode config;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static ObjectMapper<Store> mapper;
    private static final TypeSerializer<Map<Material, Map<String, Number>>> materialMapSerializer = new MaterialMapSerializer();
    private static final String configTitle = "data.shops";
    private static final Logger log = Shops.getPlugin(Shops.class).getLogger();

    public static CommentedConfigurationNode getConfig() {
        return config;
    }

    public static boolean loadConfig(Shops shops) {
        TypeToken<Map<Material, Map<String, Number>>> mapTypeToken = new TypeToken<>() {};
        TypeSerializerCollection serializers = ConfigurationOptions.defaults().serializers().childBuilder().register(mapTypeToken, materialMapSerializer).build();
        ConfigurationOptions options = ConfigurationOptions.defaults().serializers(serializers);

        File dataFolder = shops.getDataFolder();
        boolean dirsExists = dataFolder.exists();

        if (!dirsExists) dirsExists = dataFolder.mkdirs();

        if (!dirsExists) {
            log.severe("Error loading or creating data folder");
            return false;
        }

        File configFile = new File(dataFolder, configTitle);
        loader = HoconConfigurationLoader.builder().file(configFile).build();

        try {
            config = loader.load(options);
        } catch (IOException exception) {
            log.severe("Error loading config file");;
        }

        if (config != null) {
            loadShops();
            log.info("Config loaded successfully");
        }

        return config != null;
    }

    public static void saveConfig() {
        try {
            saveShops();
            loader.save(config);
            log.info("Config saved successfully");
        } catch (IOException exception) {
            log.severe("Error saving configuration");
        }
    }

    private static void loadShops() {
        try {
            mapper = ObjectMapper.factory().get(TypeToken.get(Store.class));

            for (CommentedConfigurationNode currentNode : config.node("shops").childrenList()) {
                Store store = mapper.load(currentNode);
                Store.STORES.add(store);

                log.info("ID: " + store.getUUID());
                log.info("Name: " + store.getName());
                log.info("Owner: " + store.getOwner());
                log.info("Balance: " + store.getBalance());
                log.info("Infinite Funds: " + store.hasInfiniteFunds());
                log.info("Infinite Stock: " + store.hasInfiniteStock());
                log.info("Items: " + store.getItems());
            }

            log.info(String.format("%d %s loaded", Store.STORES.size(), (Store.STORES.size() == 1) ? "shop": "shops"));
            // TODO: remove once shops are dynamically loaded
            Shops.shop = Store.STORES.get(0);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveShops() {
        try {
            CommentedConfigurationNode shopsNode = config.node("shops");

            int i = 0;

            for (Store store : Store.STORES) {
                mapper.save(store, shopsNode.node(i++));
            }

            log.info(String.format("%d %s saved", i, (Store.STORES.size() == 1) ? "shop": "shops"));
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    static class MaterialMapSerializer implements TypeSerializer<Map<Material, Map<String, Number>>> {

        private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        @Override
        public Map<Material, Map<String, Number>> deserialize(Type type, ConfigurationNode node) throws SerializationException {
            try {
                String json = node.getString("items");
                return mapper.readValue(json, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.severe("Failed to deserialize material map");
            }
            return new HashMap<>();
        }

        @Override
        public void serialize(Type type, @Nullable Map<Material, Map<String, Number>> obj, ConfigurationNode node) throws SerializationException {
            try {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                String json = mapper.writeValueAsString(obj);
                node.set(json);
            } catch (JsonProcessingException e) {
                log.severe("Failed to serialize complex map");
            }
        }

        @Override
        public @Nullable Map<Material, Map<String, Number>> emptyValue(Type specificType, ConfigurationOptions options) {
            return new HashMap<>();
        }
    }
}