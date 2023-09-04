package net.sparkzz.shops;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The Store class is instantiable and serialized/deserialized around the data.shops file
 */
@ConfigSerializable
public abstract class AbstractStore {

    /**
     * This List contains all stores that have been created
     */
    public static final ArrayList<AbstractStore> STORES = new ArrayList<>();

    @Setting private boolean infFunds = false;
    @Setting private boolean infStock = false;

    @Setting private double balance;
    @Setting private String name;
    @Setting private UUID owner;
    @Setting private UUID uuid;

    /**
     * This constructor is required for the deserializer
     *
     * @deprecated Do not use this constructor!
     */
    @Deprecated
    public AbstractStore() {}

    /**
     * Creates a store with the provided name
     *
     * @param name the name of the store to be created
     */
    public AbstractStore(String name) {
        uuid = UUID.randomUUID();
        this.name = name;

        STORES.add(this);
    }

    /**
     * Creates a store with the provided name and owner
     *
     * @param name the name of the store to be created
     * @param owner the owner's UUID to be added to the store
     */
    public AbstractStore(String name, UUID owner) {
        this(name);
        this.owner = owner;
    }

    /**
     * The identifyStore method is a common method for identifying a store based on a string name, UUID or a combination
     * using the format name~UUID
     *
     * @param nameOrUUID input name or UUID
     * @return the optional store if found or optional empty if not found or duplicates are found
     */
    public static Optional<AbstractStore> identifyStore(String nameOrUUID) throws Core.MultipleStoresMatchedException {
        List<AbstractStore> identifiedStores;
        Optional<AbstractStore> store = Optional.empty();

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
     * Get the store's unique ID
     *
     * @return the store's UUID
     */
    public UUID getUUID() {
        return uuid;
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
