package net.sparkzz.core.shops;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;

/**
 * Location based shop plugin for bukkit
 *
 * @author Brendon Butler
 */
public abstract class Shops extends JavaPlugin {

    private static boolean isTest = false;
    private static Economy econ;
    private static PluginDescriptionFile desc;
    private static Server server;

    /**
     * Default constructor for Spigot plugin
     */
    public Shops() {
        super();
        desc = getDescription();
    }

    /**
     * Constructor for MockBukkit mocking
     *
     * @param loader mocked plugin loader
     * @param description the plugin description which describes the plugin to the loader
     * @param dataFolder the data folder containing the plugin data
     * @param file the file of the plugin
     */
    public Shops(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
        isTest = true;
        desc = description;
        setMockServer(this.getServer());
    }

    /**
     * Tears down the plugin and saves configurations
     */
    @Override
    public abstract void onDisable();

    /**
     * Configures the plugin and all the plugin's resources
     */
    @Override
    public abstract void onEnable();

    /**
     * Checks whether the plugin is configured in test mode
     *
     * @return whether test mode is configured
     */
    public static boolean isTest() {
        return isTest;
    }

    /**
     * Get the configured economy configuration from Vault
     *
     * @return the economy configuration
     */
    public static Economy getEconomy() {
        return econ;
    }

    /**
     * Get the plugin description file
     *
     * @return the plugin description file
     */
    public static PluginDescriptionFile getDesc() {
        return desc;
    }

    /**
     * Get the mock server (this should only be used in test)
     *
     * @return the mock server from tests
     */
    public static Server getMockServer() {
        return server;
    }

    public static void setEconomy(Economy economy) {
        econ = economy;
    }

    /**
     * Configures the mock server for tests
     *
     * @param mockServer the mock server to be set
     */
    public static void setMockServer(Server mockServer) {
        server = mockServer;
    }

    /**
     * Exception for matching multiple Stores when expecting a single Store
     */
    public static class MultipleStoresMatchedException extends RuntimeException {
        public MultipleStoresMatchedException(String message) {
            super(message);
        }
    }
}