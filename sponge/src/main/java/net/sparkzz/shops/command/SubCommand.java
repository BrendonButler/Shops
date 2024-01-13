package net.sparkzz.shops.command;

import net.sparkzz.shops.Core;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.Notifiable;
import org.spongepowered.api.command.CommandExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interface for sub command layout
 *
 * @author Brendon Butler
 */
public abstract class SubCommand extends Notifiable implements CommandExecutor {

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
    protected static Optional<Store> identifyStore(String nameOrUUID) throws Core.MultipleStoresMatchedException {
        return Store.identifyStore(nameOrUUID);
    }

    /**
     * The identifyStores method is used to identify multiple stores based on a string name, UUID or a combination using
     * the format name~UUID
     *
     * @param nameOrUUID input name or UUID
     * @return the list of stores identified by the provided input string
     */
    protected static List<Store> identifyStores(String nameOrUUID) {
        return Store.STORES.stream()
                .filter(s -> s.getName().contains(nameOrUUID)
                             || s.getUUID().toString().contains(nameOrUUID)
                             || String.format("%s~%s", s.getName(), s.getUUID()).contains(nameOrUUID))
                .toList();
    }
}