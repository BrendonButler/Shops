package net.sparkzz.shops;

import net.milkbowl.vault.economy.Economy;
import net.sparkzz.command.CommandManager;
import net.sparkzz.util.Warehouse;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Location based shop plugin for bukkit
 *
 * @author Brendon Butler
 */
public class Shops extends JavaPlugin {

    private static Store shop;
    private static Economy econ;

    private final Logger log = getLogger();

    @Override
    public void onDisable() {
        Warehouse.saveConfig();

        log.info("Shops has been disabled!");
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            log.severe("Disabled due to missing economy dependency (see README)!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        CommandManager.registerCommands(this);

        if (!Warehouse.loadConfig(this))
            getServer().getPluginManager().disablePlugin(this);

        log.info("Shops has been enabled!");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> provider = null;

        if (getServer().getPluginManager().getPlugin("Vault") != null)
            provider = getServer().getServicesManager().getRegistration(Economy.class);

        if (provider != null)
            econ = provider.getProvider();

        return econ != null;
    }

    public static Store getDefaultShop() {
        return shop;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static void setDefaultShop(Store store) {
        shop = store;
    }
}