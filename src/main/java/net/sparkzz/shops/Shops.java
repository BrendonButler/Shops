package net.sparkzz.shops;

import net.milkbowl.vault.economy.Economy;
import net.sparkzz.command.CommandManager;
import net.sparkzz.util.InventoryManagementSystem;
import net.sparkzz.util.PointOfSale;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Location based shop plugin for bukkit
 *
 * @author Brendon Butler
 */
public class Shops extends JavaPlugin {

    public static Economy econ;
    public static InventoryManagementSystem ims;
    public static PluginDescriptionFile desc;
    public static PointOfSale pos;

    private Logger log = getLogger();

    @Override
    public void onDisable() {
        // save
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            log.severe("Disabled due to missing economy dependency (see README)!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        desc = this.getDescription();
        pos = new PointOfSale();
        ims = new InventoryManagementSystem();

        CommandManager.registerCommands(this);

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
}