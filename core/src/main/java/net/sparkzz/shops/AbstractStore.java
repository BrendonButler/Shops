package net.sparkzz.shops;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The Store class is instantiable and serialized/deserialized around the data.shops file
 */
@ConfigSerializable
public abstract class AbstractStore {

    @Setting private BigDecimal balance = BigDecimal.ZERO;
    @Setting private boolean infFunds = false;
    @Setting private boolean infStock = false;
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
    public BigDecimal getBalance() {
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
    public void addFunds(BigDecimal amount) {
        balance = balance.add(amount);
    }

    /**
     * Removes funds from the store based on the input amount
     *
     * @param amount the amount of funds to be removed from the store
     */
    public void removeFunds(BigDecimal amount) {
        if (balance.compareTo(amount) <= 0)
            balance = BigDecimal.ZERO;
        else balance = balance.subtract(amount);
    }

    /**
     * Sets the balance of the store
     *
     * @param balance the amount to set the stores balance to
     */
    public void setBalance(BigDecimal balance) {
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
