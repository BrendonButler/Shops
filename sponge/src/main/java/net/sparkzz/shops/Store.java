package net.sparkzz.shops;

import net.sparkzz.shops.util.Config;
import net.sparkzz.shops.util.Cuboid;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The Store class is instantiable and serialized/deserialized around the data.shops file
 */
@ConfigSerializable
public class Store extends AbstractStore {

    /**
     * This List contains all stores that have been created
     */
    public static final ArrayList<Store> STORES = new ArrayList<>();

    /**
     * This Map contains all default stores per world
     */
    public static final Map<@Nullable ServerWorld, Store> DEFAULT_STORES = new HashMap<>();

    // TODO: Create a map of Worlds to Cuboids to allow for multiple locations for the same store
    @Setting("location") private Cuboid cuboidLocation;
    // item : attribute, value (block_dirt : quantity, 550)
    @Setting private Map<ItemType, Map<String, Number>> items;

    /**
     * This constructor is required for the deserializer
     *
     * @deprecated Do not use this constructor!
     */
    @Deprecated
    public Store() {}

    /**
     * Creates a store with the provided name
     *
     * @param name the name of the store to be created
     */
    public Store(String name) {
        super(name);
        items = new HashMap<>();

        STORES.add(this);
    }

    /**
     * Creates a store with the provided name and owner
     *
     * @param name the name of the store to be created
     * @param owner the owner's UUID to be added to the store
     */
    public Store(String name, UUID owner) {
        super(name, owner);
        items = new HashMap<>();

        STORES.add(this);
    }

    /**
     * Creates a store with the provided name, owner, and cuboid location
     *
     * @param name the name of the store to be created
     * @param owner the owner's UUID to be added to the store
     * @param cuboidLocation the Cuboid location where this store is located
     */
    public Store(String name, UUID owner, Cuboid cuboidLocation) {
        super(name, owner);
        items = new HashMap<>();
        this.cuboidLocation = cuboidLocation;

        STORES.add(this);
    }

    /**
     * Gets a list of Stores
     *
     * @return the list of stores
     */
    public static ArrayList<Store> getStores() {
        return STORES;
    }

    /**
     * Gets the default store based on the input world
     *
     * @param world the world to check for the default store
     * @return the default store based on the world
     */
    public static Optional<Store> getDefaultStore(@Nullable ServerWorld world) {
        Optional<Store> nullDefaultStore = Optional.ofNullable(DEFAULT_STORES.get(null));

        return nullDefaultStore.isPresent() ? nullDefaultStore : Optional.ofNullable(DEFAULT_STORES.get(world));
    }

    /**
     * Sets the default store(s) per world (1 default store per world)
     *
     * @param store the default store to be set
     */
    public static void setDefaultStore(ServerWorld world, Store store) {
        DEFAULT_STORES.put(world, store);
        Config.setDefaultStore(world, store);
    }

    /**
     * The identifyStore method is a common method for identifying a store based on a string name, UUID or a combination
     * using the format name~UUID
     *
     * @param nameOrUUID input name or UUID
     * @return the optional store if found or optional empty if not found or duplicates are found
     */
    public static Optional<Store> identifyStore(String nameOrUUID) throws Core.MultipleStoresMatchedException {
        List<Store> identifiedStores;
        Optional<Store> store = Optional.empty();

        if (nameOrUUID.contains("~")) {
            String[] input = nameOrUUID.split("~");

            identifiedStores = STORES.stream().filter(s -> s.getName().equalsIgnoreCase(input[0]) && s.getUUID().toString().equalsIgnoreCase(input[1])).collect(Collectors.toCollection(ArrayList::new));
        } else {
            identifiedStores = STORES.stream().filter(s -> s.getName().equalsIgnoreCase(nameOrUUID) || s.getUUID().toString().equalsIgnoreCase(nameOrUUID)).collect(Collectors.toCollection(ArrayList::new));
        }

        if (identifiedStores.size() == 1)
            store = Optional.of(identifiedStores.get(0));
        else if (identifiedStores.size() > 1) throw new Core.MultipleStoresMatchedException("Multiple Stores matched");

        return store;
    }

    /**
     * Check if the store contains the provided material
     *
     * @param material the material to be identified within the store
     * @return whether the store contains the provided material
     */
    public boolean containsItemType(ItemType material) {
        return items.containsKey(material);
    }

    /**
     * Checks the buy price of a material
     *
     * @param material the material to be queried for its buy price
     * @return the buy price of the provided material
     */
    public BigDecimal getBuyPrice(ItemType material) {
        return (items.containsKey(material) ? BigDecimal.valueOf(items.get(material).get("buy").doubleValue()) : BigDecimal.valueOf(-1D));
    }

    /**
     * Gets the cuboid location of the store
     *
     * @return the cuboid location of the store
     */
    public Cuboid getCuboidLocation() {
        return cuboidLocation;
    }

    /**
     * Checks the sell price of a material
     *
     * @param material the material to be queried for its sell price
     * @return the sell price of the provided material
     */
    public BigDecimal getSellPrice(ItemType material) {
        return (items.containsKey(material) ? BigDecimal.valueOf(items.get(material).get("sell").doubleValue()) : BigDecimal.valueOf(-1D));
    }

    /**
     * Get the items within the store with their attributes
     *
     * @return the items and attributes within the store
     */
    public Map<ItemType, Map<String, Number>> getItems() {
        return items;
    }

    /**
     * Get the attributes of a material in the store
     *
     * @param material the material to be queried for its attributes
     * @return the attributes of the provided material
     */
    public Map<String, Number> getAttributes(ItemType material) {
        return items.get(material);
    }

    /**
     * Add an item stack to the store
     *
     * @param itemStack the item stack to be added to the store
     */
    public void addItem(ItemStack itemStack) {
        addItem(itemStack.type(), itemStack.quantity());
    }

    /**
     * Add a material and quantity to the store
     *
     * @param material the material to be added to the store
     * @param quantity the quantity of the provided material to be added to the store
     */
    public void addItem(ItemType material, int quantity) {
        if (items.containsKey(material)) {
            int currentQuantity = items.get(material).get("quantity").intValue();
            items.get(material).put("quantity", currentQuantity + quantity);
        } else {
            Map<String, Number> attributes = new HashMap<>();

            attributes.put("quantity", quantity);
            attributes.put("max_quantity", -1);
            attributes.put("buy", -1D);
            attributes.put("sell", -1D);

            items.put(material, attributes);
        }
    }

    /**
     * Adds a new item to the store
     *
     * @param material the material to be added to the store
     * @param quantity the quantity of the provided material to be added to the store
     * @param maxQuantity the max quantity of the provided material
     * @param buyValue the buy value of the provided material
     * @param sellValue the sell value of the provided material
     */
    public void addItem(ItemType material, int quantity, int maxQuantity, BigDecimal buyValue, BigDecimal sellValue) {
        addItem(material, quantity);

        items.get(material).put("max_quantity", maxQuantity);
        items.get(material).put("buy", buyValue);
        items.get(material).put("sell", sellValue);
    }

    /**
     * Removes an item stack from the store (not the entire item entry)
     *
     * @param itemStack the item stack to be removed from the store
     */
    public void removeItem(ItemStack itemStack) {
        removeItem(itemStack.type(), itemStack.quantity());
    }

    /**
     * Removes an item from the store's selection
     *
     * @param material the material to be removed from the store
     */
    public void removeItem(ItemType material) {
        items.remove(material);
    }

    /**
     * Removes the provided quantity of a provided material from the store
     *
     * @param material the material to be removed from the store
     * @param quantity the quantity of the provided material to be removed from the store
     */
    public void removeItem(ItemType material, int quantity) {
        Map<String, Number> attributes = items.get(material);
        int newQuantity = attributes.get("quantity").intValue() - quantity;

        attributes.put("quantity", newQuantity);
        items.put(material, attributes);
    }

    /**
     * Sets material attributes in a store
     *
     * @param material the material to be set
     * @param attribute the attribute to be set
     * @param value the value of the attribute to be set
     */
    public void setAttribute(ItemType material, String attribute, Number value) {
        items.get(material).put(attribute, value);
    }

    /**
     * Sets the bounds of the store based on the Cuboid inputted
     *
     * @param cuboid the store bounds defined by a cuboid
     */
    public void setCuboidLocation(Cuboid cuboid) {
        this.cuboidLocation = cuboid;
    }
}
