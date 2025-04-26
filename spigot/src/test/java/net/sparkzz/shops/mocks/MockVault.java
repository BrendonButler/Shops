package net.sparkzz.shops.mocks;

import com.earth2me.essentials.economy.vault.VaultEconomyProvider;
import net.milkbowl.vault.economy.Economy;
import net.sparkzz.shops.util.VaultProvider;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

public class MockVault extends JavaPlugin implements VaultProvider {

    protected MockVault(
            JavaPluginLoader loader,
            PluginDescriptionFile description,
            File dataFolder,
            File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        Logger log = this.getLogger();
        ServicesManager servicesManager = this.getServer().getServicesManager();
        Economy econ = null;

        // mock econ
        try {
            econ = VaultEconomyProvider.class.getConstructor(Plugin.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {

        }

        assert econ != null;
        servicesManager.register(Economy.class, econ, this, ServicePriority.Low);
    }
}
