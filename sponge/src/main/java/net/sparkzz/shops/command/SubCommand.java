package net.sparkzz.shops.command;

import net.sparkzz.shops.Core;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.Notifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interface for sub command layout
 *
 * @author Brendon Butler
 */
public abstract class SubCommand extends Notifiable {

    /**
     * Holds any stores that are identified based on the identifyStore method
     */
    protected List<Store> stores = new ArrayList<>();

    /**
     * The identifyStore method is a common method for identifying a store based on a string name, UUID or a combination
     * using the format name~UUID
     *
     * @param nameOrUUID input name or UUID
     * @return the optional store if found or optional empty if not found or duplicates are found
     */
    protected Optional<Store> identifyStore(String nameOrUUID) throws Core.MultipleStoresMatchedException {
        return Store.identifyStore(nameOrUUID);
    }
}