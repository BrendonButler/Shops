package net.sparkzz.shops;

import net.sparkzz.command.CommandManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Location based shop plugin for bukkit
 *
 * @author Brendon Butler
 */
public class Shops extends JavaPlugin {

    public static PluginDescriptionFile desc;

    private Logger log;

    @Override
    public void onDisable() {
        // save
    }

    @Override
    public void onEnable() {
        desc = this.getDescription();
        log = getLogger();

        CommandManager.registerCommands(this);

        log.info("Shops has been enabled!");
    }
}