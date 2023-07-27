package net.sparkzz.command;

import net.sparkzz.shops.Store;
import net.sparkzz.util.Notifiable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    protected Optional<Store> identifyStore(String nameOrUUID) {
        Optional<Store> store = Optional.empty();

        if (nameOrUUID.contains("~")) {
            String[] input = nameOrUUID.split("~");

            stores = Store.STORES.stream().filter(s -> s.getName().equalsIgnoreCase(input[0]) && s.getUUID().toString().equalsIgnoreCase(input[1])).collect(Collectors.toCollection(ArrayList::new));
        } else {
            stores = Store.STORES.stream().filter(s -> s.getName().equalsIgnoreCase(nameOrUUID) || s.getUUID().toString().equalsIgnoreCase(nameOrUUID)).collect(Collectors.toCollection(ArrayList::new));
        }

        if (stores.size() == 1)
            store = Optional.of(stores.get(0));

        return store;
    }
}