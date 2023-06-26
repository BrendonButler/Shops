package net.sparkzz.shops;

import net.milkbowl.vault.economy.Economy;
import net.sparkzz.command.CommandManager;
import net.sparkzz.util.Warehouse;
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

    public static Store shop;
    public static Economy econ;
    public static PluginDescriptionFile desc;

    private final Logger log = getLogger();
    private boolean isTest = false;

    public Shops() {
        super();
    }

    protected Shops(
            JavaPluginLoader loader,
            PluginDescriptionFile description,
            File dataFolder,
            File file) {
        super(loader, description, dataFolder, file);
        isTest = true;
    }

    @Override
    public void onDisable() {
        if (!isTest) Warehouse.saveConfig();

        log.info("Shops has been disabled!");
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            log.severe("Disabled due to missing economy dependency (see README)!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        desc = this.getDescription();

        CommandManager.registerCommands(this);

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
}