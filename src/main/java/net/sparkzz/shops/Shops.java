package net.sparkzz.shops;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Location based shop plugin for bukkit
 *
 * @author Brendon Butler
 */
public class Shops extends JavaPlugin {

    private Logger log;

    @Override
    public void onDisable() {
        // save
    }

    @Override
    public void onEnable() {
        log = getLogger();

        log.info("Shops has been enabled!");
    }
}