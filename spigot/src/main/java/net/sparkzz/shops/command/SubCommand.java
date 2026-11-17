package net.sparkzz.shops.command;

import net.sparkzz.shops.Core;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.AbstractNotifier;
import net.sparkzz.shops.util.Notifiable;
import net.sparkzz.shops.util.Notifier;
import net.sparkzz.shops.util.Permissions;
import net.sparkzz.shops.util.ShopAction;
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
     * The identifyStore method is a common method for identifying a store based on a string name, UUID or a combination
     * using the format name~UUID
     *
     * @param nameOrUUID input name or UUID
     * @return the optional store if found or optional empty if not found or duplicates are found
     */
    protected Optional<Store> identifyStore(String nameOrUUID) throws Core.MultipleStoresMatchedException {
        return Store.identifyStore(nameOrUUID);
    }

    /**
     * Notifies the sender when denied for no-store actions
     *
     * @param sender The sender to check and notify
     * @param action The action attempting to be performed
     * @return true if denied; false if permitted
     */
    protected boolean isDenied(CommandSender sender, ShopAction action) {
        Optional<AbstractNotifier.CipherKey> denial = Permissions.denialFor(sender, action);

        denial.ifPresent(key -> Notifier.process(sender, key, getAttributes()));
        return denial.isPresent();
    }

    /**
     * Notifies the sender when denied for specific store actions
     *
     * @param sender The sender to check and notify
     * @param action The action attempting to be performed
     * @param store  The store to check against
     * @return true if denied; false if permitted
     */
    protected boolean isDenied(CommandSender sender, ShopAction action, Store store) {
        Optional<AbstractNotifier.CipherKey> denial = Permissions.denialFor(sender, action, store);

        denial.ifPresent(key -> Notifier.process(sender, key, getAttributes()));
        return denial.isPresent();
    }

    /**
     * Overload method to check the default action for the SubCommand
     *
     * @param sender The sender to check and notify
     * @param store  The store to check against
     * @return true if denied; false if permitted
     */
    protected boolean isDenied(CommandSender sender, Store store) {
        return isDenied(sender, getAction(), store);
    }

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
     * The getAction method is used to identify the action that the subcommand is performing for permissions
     *
     * @return the ShopAction being performed
     */
    public abstract ShopAction getAction();
}