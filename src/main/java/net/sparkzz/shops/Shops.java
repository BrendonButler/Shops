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
        setupEconomy();
        /*if (!setupEconomy()) {
            log.severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }*/

        desc = this.getDescription();
        pos = new PointOfSale();
        ims = new InventoryManagementSystem();

        CommandManager.registerCommands(this);

        log.info("Shops has been enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) return false;
        econ = rsp.getProvider();

        return econ != null;
    }
}