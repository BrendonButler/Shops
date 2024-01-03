package net.sparkzz.shops;

import com.google.inject.Inject;
import net.sparkzz.shops.event.EntranceListener;
import net.sparkzz.shops.util.Notifier;
import net.sparkzz.shops.util.Warehouse;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

/**
 * Location based shop plugin for Sponge
 *
 * @author Brendon Butler
 */
@Plugin("shops")
public class Shops {

    @Inject
    private Logger logger;

    private boolean pluginLoaded = false;

    @Listener
    public void onServerStart(final StartedEngineEvent<Server> event) {
        pluginLoaded = Warehouse.loadConfig();

        if (!pluginLoaded) {
            logger.info("Shops failed to load properly!");
            return;
        }

        Sponge.eventManager().registerListeners(this.container, new EntranceListener());
        Notifier.loadCustomMessages();

        logger.info("Shops has been enabled!");
    }

    @Listener
    public void onServerStop(final StoppingEngineEvent<Server> event) {
        if (!pluginLoaded) return;

        Warehouse.saveConfig();

        logger.info("Shops has been disabled!");
    }

    @Listener
    public void onPluginRefresh(final RefreshGameEvent event) {
        pluginLoaded = Warehouse.loadConfig();

        if (!pluginLoaded) return;

        logger.info("Shops configuration has been reloaded!");
    }

    @Inject
    PluginContainer container;
}