package net.sparkzz.shops;

import net.milkbowl.vault.economy.Economy;
import net.sparkzz.shops.command.CommandManager;
import net.sparkzz.shops.event.EntranceListener;
import net.sparkzz.shops.util.Notifier;
import net.sparkzz.shops.util.Warehouse;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.logging.Logger;

import static net.sparkzz.shops.Core.isTest;

/**
 * Location based shop plugin for Spigot/Bukkit
 *
 * @author Brendon Butler
 */
public class Shops extends JavaPlugin {

    private static Economy economy;
    private static Logger log;
    private static PluginDescriptionFile description;
    private static Server server;


    /**
     * Default constructor for Spigot plugin
     */
    public Shops() {
        super();
        Shops.log = getLogger();
        Shops.description = getDescription();
    }

    /**
     * Constructor for MockBukkit mocking
     *
     * @param loader mocked plugin loader
     * @param description the plugin description which describes the plugin to the loader
     * @param dataFolder the data folder containing the plugin data
     * @param file the file of the plugin
     */
    protected Shops(JavaPluginLoader loader,
                    PluginDescriptionFile description,
                    File dataFolder,
                    File file) {
        super(loader, description, dataFolder, file);
        Core.setTest();
        Shops.log = getLogger();
        Shops.description = description;
    }

    /**
     * Gets the logger
     *
     * @return the logger
     */
    public static Logger getLog() {
        return log;
    }

    /**
     * Gets the plugin description
     *
     * @return the plugin description
     */
    public static PluginDescriptionFile getDesc() {
        return description;
    }

    /**
     * Gets the server, this is necessary for the MockBukkit configuration
     *
     * @return the server instance
     */
    public static Server getServerInstance() {
        return (server == null) ? Shops.getPlugin(Shops.class).getServer() : server;
    }

    /**
     * Sets the server, this is necessary for the MockBukkit configuration
     *
     * @param server the server instance to be set
     */
    public static void setServerInstance(Server server) {
        Shops.server = server;
    }

    /**
     * Tears down the plugin and saves configurations
     */
    @Override
    public void onDisable() {
        if (!isTest() && getEconomy() != null) Warehouse.saveConfig();

        getLogger().info("Shops has been disabled!");
    }

    /**
     * Configures the plugin and all the plugin's resources
     */
    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to missing economy dependency (see README)!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        server = getServer();
        CommandManager.registerCommands(this);
        getServer().getPluginManager().registerEvents(new EntranceListener(), this);

        if (!isTest() && !Warehouse.loadConfig(this))
            getServer().getPluginManager().disablePlugin(this);

        Notifier.loadCustomMessages();

        getLogger().info("Shops has been enabled!");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> provider = null;

        if (isTest() || getServer().getPluginManager().getPlugin("Vault") != null)
            provider = getServer().getServicesManager().getRegistration(Economy.class);

        if (provider != null)
            setEconomy(provider.getProvider());

        return getEconomy() != null;
    }

    /**
     * Gets the configured economy configuration from Vault
     *
     * @return the economy configuration
     */
    public static Economy getEconomy() {
        return economy;
    }

    /**
     * Sets the economy provider
     *
     * @param economy the economy provider to be set
     */
    public static void setEconomy(Economy economy) {
        Shops.economy = economy;
    }
}