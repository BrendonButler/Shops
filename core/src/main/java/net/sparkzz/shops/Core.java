package net.sparkzz.shops;

public class Core {

    private static boolean test = false;

    /**
     * Checks whether the plugin is configured in test mode
     *
     * @return whether test mode is configured
     */
    public static boolean isTest() {
        return test;
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
