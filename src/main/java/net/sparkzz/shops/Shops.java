package net.sparkzz.shops;

import net.milkbowl.vault.economy.Economy;
import net.sparkzz.command.CommandManager;
import net.sparkzz.util.PointOfSale;
import org.bukkit.Material;
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

    public static Store shop;
    public static Economy econ;
    public static PluginDescriptionFile desc;
    public static PointOfSale pos;

    private final Logger LOG = getLogger();

    @Override
    public void onDisable() {
        // TODO: save
        LOG.info("Shops has been disabled!");
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            LOG.severe("Disabled due to missing economy dependency (see README)!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        desc = this.getDescription();
        pos = new PointOfSale();

        CommandManager.registerCommands(this);

        testShop();

        LOG.info("Shops has been enabled!");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> provider = null;

        if (getServer().getPluginManager().getPlugin("Vault") != null)
            provider = getServer().getServicesManager().getRegistration(Economy.class);

        if (provider != null)
            econ = provider.getProvider();

        return econ != null;
    }

    private void testShop() {
        shop = new Store("DEFAULT");
        shop.addItem(Material.matchMaterial("ACACIA_BUTTON"), 10, 64, 1.5D, 0.5D);
        shop.addItem(Material.matchMaterial("OBSIDIAN"), 10, 64, 5D, 2D);
        shop.setInfiniteFunds(true);
    }
}