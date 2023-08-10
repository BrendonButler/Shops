package net.sparkzz.shops;

import net.milkbowl.vault.economy.Economy;
import net.sparkzz.command.CommandManager;
import net.sparkzz.event.EntranceListener;
import net.sparkzz.util.Warehouse;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.logging.Logger;

/**
 * Location based shop plugin for bukkit
 *
 * @author Brendon Butler
 */
public class Shops extends JavaPlugin {

    private static boolean isTest = false;
    private static Server server;
    private static Economy econ;
    private static PluginDescriptionFile desc;

    private final Logger log = getLogger();

    /**
     * Default constructor for Spigot plugin
     */
    public Shops() {
        super();
    }

    /**
     * Constructor for MockBukkit mocking
     *
     * @param loader mocked plugin loader
     * @param description the plugin description which describes the plugin to the loader
     * @param dataFolder the data folder containing the plugin data
     * @param file the file of the plugin
     */
    protected Shops(
            JavaPluginLoader loader,
            PluginDescriptionFile description,
            File dataFolder,
            File file) {
        super(loader, description, dataFolder, file);
        isTest = true;
    }

    /**
     * Tears down the plugin and saves configurations
     */
    @Override
    public void onDisable() {
        if (!isTest) Warehouse.saveConfig();

        log.info("Shops has been disabled!");
    }

    /**
     * Configures the plugin and all the plugin's resources
     */
    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            log.severe("Disabled due to missing economy dependency (see README)!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        desc = this.getDescription();

        CommandManager.registerCommands(this);
        getServer().getPluginManager().registerEvents(new EntranceListener(), this);

        if (!isTest && !Warehouse.loadConfig(this))
            getServer().getPluginManager().disablePlugin(this);

        log.info("Shops has been enabled!");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> provider = null;

        if (isTest || getServer().getPluginManager().getPlugin("Vault") != null)
            provider = getServer().getServicesManager().getRegistration(Economy.class);

        if (provider != null)
            econ = provider.getProvider();

        return econ != null;
    }

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
     * Get the plugin description from Shops
     *
     * @return the plugin description
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

    /**
     * Configures the mock server for tests
     *
     * @param mockServer the mock server to be set
     */
    public static void setMockServer(Server mockServer) {
        server = mockServer;
    }
}