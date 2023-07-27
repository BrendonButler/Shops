package net.sparkzz.shops;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Store class is instantiable and serialized/deserialized around the data.shops file
 */
@ConfigSerializable
public class Store {

    /**
     * This List contains all stores that have been created
     */
    public static final ArrayList<Store> STORES = new ArrayList<>();

    @Setting private boolean infFunds = false;
    @Setting private boolean infStock = false;
    @Setting private double balance;
    @Setting private String name;
    // item : attribute, value (block_dirt : quantity, 550)
    @Setting private Map<Material, Map<String, Number>> items;
    @Setting private UUID owner;
    @Setting private UUID uuid;

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
        uuid = UUID.randomUUID();
        items = new HashMap<>();
        this.name = name;

        STORES.add(this);
    }

    /**
     * Creates a store with the provided name
     *
     * @param name the name of the store to be created
     * @param owner the owner's UUID to be added to the store
     */
    public Store(String name, UUID owner) {
        this(name);
        this.owner = owner;
    }

    /**
     * Check if the store contains the provided material
     *
     * @param material the material to be identified within the store
     * @return whether the store contains the provided material
     */
    public boolean containsMaterial(Material material) {
        return items.containsKey(material);
    }

    /**
     * Checks if the store has the infinite funds flag set
     *
     * @return whether the store has infinite funds
     */
    public boolean hasInfiniteFunds() {
        return infFunds;
    }

    /**
     * Checks if the store has the infinite stock flag set
     *
     * @return whether the store has infinite stock
     */
    public boolean hasInfiniteStock() {
        return infStock;
    }

    /**
     * Checks the balance of the store
     *
     * @return the balance of the store
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Checks the buy price of a material
     *
     * @param material the material to be queried for its buy price
     * @return the buy price of the provided material
     */
    public double getBuyPrice(Material material) {
        return (items.containsKey(material) ? items.get(material).get("buy").doubleValue() : -1D);
    }

    /**
     * Checks the sell price of a material
     *
     * @param material the material to be queried for its sell price
     * @return the sell price of the provided material
     */
    public double getSellPrice(Material material) {
        return (items.containsKey(material) ? items.get(material).get("sell").doubleValue() : -1D);
    }

    /**
     * Get the store's unique ID
     *
     * @return the store's UUID
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the items within the store with their attributes
     *
     * @return the items and attributes within the store
     */
    public Map<Material, Map<String, Number>> getItems() {
        return items;
    }

    /**
     * Get the attributes of a material in the store
     *
     * @param material the material to be queried for its attributes
     * @return the attributes of the provided material
     */
    public Map<String, Number> getAttributes(Material material) {
        return items.get(material);
    }

    /**
     * Get the name of the store
     *
     * @return the store name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the store owner's UUID
     *
     * @return the UUID of the store owner
     */
    public UUID getOwner() {
        return owner;
    }

    /**
     * Add funds to the store
     *
     * @param amount the amount of funds to be added to the store
     */
    public void addFunds(double amount) {
        balance += amount;
    }

    /**
     * Add an item stack to the store
     *
     * @param itemStack the item stack to be added to the store
     */
    public void addItem(ItemStack itemStack) {
        addItem(itemStack.getType(), itemStack.getAmount());
    }

    /**
     * Add a material and quantity to the store
     *
     * @param material the material to be added to the store
     * @param quantity the quantity of the provided material to be added to the store
     */
    public void addItem(Material material, int quantity) {
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
    public void addItem(Material material, int quantity, int maxQuantity, double buyValue, double sellValue) {
        addItem(material, quantity);

        items.get(material).put("max_quantity", maxQuantity);
        items.get(material).put("buy", buyValue);
        items.get(material).put("sell", sellValue);
    }

    /**
     * Removes funds from the store based on the input amount
     *
     * @param amount the amount of funds to be removed from the store
     */
    public void removeFunds(double amount) {
        if (balance <= amount)
            balance = 0;
        else balance -= amount;
    }

    /**
     * Removes an item stack from the store (not the entire item entry)
     *
     * @param itemStack the item stack to be removed from the store
     */
    public void removeItem(ItemStack itemStack) {
        removeItem(itemStack.getType(), itemStack.getAmount());
    }

    /**
     * Removes an item from the store's selection
     *
     * @param material the material to be removed from the store
     */
    public void removeItem(Material material) {
        items.remove(material);
    }

    /**
     * Removes the provided quantity of a provided material from the store
     *
     * @param material the material to be removed from the store
     * @param quantity the quantity of the provided material to be removed from the store
     */
    public void removeItem(Material material, int quantity) {
        Map<String, Number> attributes = items.get(material);
        int newQuantity = attributes.get("quantity").intValue() - quantity;

        attributes.put("quantity", newQuantity);
        items.put(material, attributes);
    }

    /**
     * Sets the balance of the store
     *
     * @param balance the amount to set the stores balance to
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * Sets the infinite funds flag based on the input value
     *
     * @param value the value to be applied to the infinite funds flag
     */
    public void setInfiniteFunds(boolean value) {
        infFunds = value;
    }

    /**
     * Sets the infinite stock flag based on the input value
     *
     * @param value the value to be applied to the infinite stock flag
     */
    public void setInfiniteStock(boolean value) {
        infStock = value;
    }

    /**
     * Sets the name of the store to the input name
     *
     * @param name the name to be set for the store
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the stores owner UUID to the input unique id
     *
     * @param uuid the unique id of the new store owner
     */
    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    /**
     * Generates the string of the store's attributes
     *
     * @return the name of the store as the toString result
     */
    @Override
    public String toString() {
        return this.name;
    }
}
