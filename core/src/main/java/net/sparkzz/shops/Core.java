package net.sparkzz.shops;

import net.milkbowl.vault.economy.Economy;

import java.util.logging.Logger;

public class Core {

    private static boolean test = false;
    private static Economy econ;
    private static Logger logger;

    /**
     * Checks whether the plugin is configured in test mode
     *
     * @return whether test mode is configured
     */
    public static boolean isTest() {
        return test;
    }

    /**
     * Gets the configured economy configuration from Vault
     *
     * @return the economy configuration
     */
    public static Economy getEconomy() {
        return econ;
    }

    /**
     * Gets the logger
     *
     * @return the logger
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Sets the economy provider
     *
     * @param economy the economy provider to be set
     */
    public static void setEconomy(Economy economy) {
        Core.econ = economy;
    }

    /**
     * Sets the logger
     *
     * @param logger the logger to be set
     */
    public static void setLogger(Logger logger) {
        Core.logger = logger;
    }

    /**
     * Sets the test flag for indicating that tests are running
     */
    public static void setTest() {
        Core.test = true;
    }

    /**
     * Exception for matching multiple Stores when expecting a single Store
     */
    public static class MultipleStoresMatchedException extends RuntimeException {
        public MultipleStoresMatchedException(String message) {
            super(message);
        }
    }
}
