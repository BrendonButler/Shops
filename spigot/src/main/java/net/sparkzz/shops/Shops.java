package net.sparkzz.shops;

import net.milkbowl.vault.economy.Economy;
import net.sparkzz.core.command.CommandManager;
import net.sparkzz.core.event.EntranceListener;
import net.sparkzz.core.util.Notifier;
import net.sparkzz.core.util.Warehouse;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.logging.Logger;

/**
 * Location based shop plugin for bukkit
 *
 * @author Brendon Butler
 */
public class Shops extends net.sparkzz.core.shops.Shops {

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
    }

    /**
     * Tears down the plugin and saves configurations
     */
    @Override
    public void onDisable() {
        if (!isTest() && getEconomy() != null) Warehouse.saveConfig();

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

        CommandManager.registerCommands(this);
        getServer().getPluginManager().registerEvents(new EntranceListener(), this);

        if (!isTest() && !Warehouse.loadConfig(this))
            getServer().getPluginManager().disablePlugin(this);

        Notifier.loadCustomMessages();

        log.info("Shops has been enabled!");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> provider = null;

        if (isTest() || getServer().getPluginManager().getPlugin("Vault") != null)
            provider = getServer().getServicesManager().getRegistration(Economy.class);

        if (provider != null)
            setEconomy(provider.getProvider());

        return getEconomy() != null;
    }
}