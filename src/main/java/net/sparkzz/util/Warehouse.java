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

import static net.sparkzz.shops.Store.STORES;

/**
 * Helper class to manage saving and loading of Shops
 *
 * @author Brendon Butler
 */
public class Warehouse {

    private static CommentedConfigurationNode config;
    private static ConfigurationLoader<CommentedConfigurationNode> loader;
    private static ObjectMapper<Store> mapper;
    private static final Logger log = Shops.getPlugin(Shops.class).getLogger();
    private static final String configTitle = "data.shops";
    private static final TypeSerializer<Map<Material, Map<String, Number>>> materialMapSerializer = new MaterialMapSerializer();

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

            if (config == null) throw new IOException();
        } catch (IOException exception) {
            log.severe("Error loading config file, disabling Shops plugin");
            return false;
        }

        loadShops();
        log.info("Config loaded successfully");

        return true;
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

            for (CommentedConfigurationNode currentNode : config.node("shops").childrenList())
                STORES.add(mapper.load(currentNode));

            log.info(String.format("%d %s loaded", STORES.size(), (STORES.size() == 1) ? "shop": "shops"));
            // TODO: remove once shops are dynamically loaded
            Shops.setDefaultShop(STORES.get(0));
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveShops() {
        try {
            CommentedConfigurationNode shopsNode = config.node("shops");

            // Clear the existing shops before saving the updated list
            shopsNode.childrenMap().keySet().forEach(shopsNode::removeChild);

            int i = 0;

            for (Store store : STORES)
                mapper.save(store, shopsNode.node(i++));

            log.info(String.format("%d %s saved", i, (STORES.size() == 1) ? "shop" : "shops"));
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    static class MaterialMapSerializer implements TypeSerializer<Map<Material, Map<String, Number>>> {

        private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

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

        @Override
        public void serialize(Type type, @Nullable Map<Material, Map<String, Number>> obj, ConfigurationNode node) throws SerializationException {
            try {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                String json = mapper.writeValueAsString(obj);
                node.set(json);
            } catch (JsonProcessingException e) {
                log.severe("Failed to serialize material map");
            }
        }

        @Override
        public @Nullable Map<Material, Map<String, Number>> emptyValue(Type specificType, ConfigurationOptions options) {
            return new HashMap<>();
        }
    }
}
