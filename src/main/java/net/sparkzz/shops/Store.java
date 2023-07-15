package net.sparkzz.shops;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class Store {

    public static final ArrayList<Store> STORES = new ArrayList<>();

    @Setting
    private UUID uuid;

    // item : attribute, value (block_dirt : quantity, 550)
    @Setting
    private Map<Material, Map<String, Number>> items;

    @Setting private boolean infFunds = false;
    @Setting private boolean infStock = false;
    @Setting private double balance;
    @Setting private String name;
    @Setting private UUID owner;

    /**
     * This constructor is required for the deserializer
     *
     * @deprecated Do not use this constructor!
     */
    @Deprecated
    public Store() {}

    public Store(String name) {
        uuid = UUID.randomUUID();
        items = new HashMap<>();
        this.name = name;

        STORES.add(this);
    }

    public Store(String name, UUID owner) {
        this(name);
        this.owner = owner;
    }

    public boolean containsMaterial(Material material) {
        return items.containsKey(material);
    }

    public boolean hasInfiniteFunds() {
        return infFunds;
    }

    public boolean hasInfiniteStock() {
        return infStock;
    }

    public double getBalance() {
        return balance;
    }

    public double getBuyPrice(Material material) {
        return (items.containsKey(material) ? items.get(material).get("buy").doubleValue() : -1D);
    }

    public double getSellPrice(Material material) {
        return (items.containsKey(material) ? items.get(material).get("sell").doubleValue() : -1D);
    }

    public UUID getUUID() {
        return uuid;
    }

    public Map<Material, Map<String, Number>> getItems() {
        return items;
    }

    public Map<String, Number> getAttributes(Material material) {
        return items.get(material);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void addFunds(double amount) {
        balance += amount;
    }

    public void addItem(ItemStack itemStack) {
        addItem(itemStack.getType(), itemStack.getAmount());
    }

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

    public void addItem(Material material, int quantity, int maxQuantity, double buyValue, double sellValue) {
        addItem(material, quantity);

        items.get(material).put("max_quantity", maxQuantity);
        items.get(material).put("buy", buyValue);
        items.get(material).put("sell", sellValue);
    }

    public void removeFunds(double amount) {
        if (balance <= amount)
            balance = 0;
        else balance -= amount;
    }

    public void removeItem(ItemStack itemStack) {
        removeItem(itemStack.getType(), itemStack.getAmount());
    }

    public void removeItem(Material material) {
        items.remove(material);
    }

    public void removeItem(Material material, int quantity) {
        Map<String, Number> attributes = items.get(material);
        int newQuantity = attributes.get("quantity").intValue() - quantity;

        attributes.put("quantity", newQuantity);
        items.put(material, attributes);
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setInfiniteFunds(boolean value) {
        infFunds = value;
    }

    public void setInfiniteStock(boolean value) {
        infStock = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
