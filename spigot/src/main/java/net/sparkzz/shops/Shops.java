package net.sparkzz.shops;

import net.milkbowl.vault.economy.Economy;
import net.sparkzz.shops.command.CommandManager;
import net.sparkzz.shops.event.EntranceListener;
import net.sparkzz.shops.util.Notifier;
import net.sparkzz.shops.util.Warehouse;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Location based shop plugin for Spigot/Bukkit
 *
 * @author Brendon Butler
 */
public class Shops extends JavaPlugin {

    private static Economy economy;
    private static Logger log;
    private static PluginDescriptionFile description;

    /**
     * Default constructor for Spigot plugin
     */
    public Shops() {
        super();
        Shops.log = getLogger();
        Shops.description = getDescription();
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
     * Tears down the plugin and saves configurations
     */
    @Override
    public void onDisable() {
        if (getEconomy() != null) Warehouse.saveConfig();

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

        CommandManager.registerCommands(this);
        getServer().getPluginManager().registerEvents(new EntranceListener(), this);

        if (!Warehouse.loadConfig(this)) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Notifier.loadCustomMessages();

        getLogger().info("Shops has been enabled!");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> provider = null;

        if (getServer().getPluginManager().getPlugin("Vault") != null)
            provider = getServer().getServicesManager().getRegistration(Economy.class);

        setEconomy(provider != null ? provider.getProvider() : null);

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