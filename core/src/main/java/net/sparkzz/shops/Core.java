package net.sparkzz.shops;

public class Core {

    /**
     * Exception for matching multiple Stores when expecting a single Store
     */
    public static class MultipleStoresMatchedException extends RuntimeException {
        public MultipleStoresMatchedException(String message) {
            super(message);
        }
    }
}
