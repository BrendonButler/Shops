package net.sparkzz.core.command;

import net.sparkzz.core.shops.Shops;
import net.sparkzz.core.shops.Store;
import net.sparkzz.core.util.Notifiable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
     * The process method is where the subcommands are built
     *
     * @param sender the sender attempting the command
     * @param command the command to be processed
     * @param label the command label
     * @param args the arguments following the command
     * @return whether the command was successful
     */
    public abstract boolean process(CommandSender sender, Command command, String label, String[] args);

    /**
     * The identifyStore method is a common method for identifying a store based on a string name, UUID or a combination
     * using the format name~UUID
     *
     * @param nameOrUUID input name or UUID
     * @return the optional store if found or optional empty if not found or duplicates are found
     */
    protected Optional<Store> identifyStore(String nameOrUUID) throws Shops.MultipleStoresMatchedException {
        return Store.identifyStore(nameOrUUID);
    }
}