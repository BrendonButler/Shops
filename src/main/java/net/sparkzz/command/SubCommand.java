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

    protected List<Store> stores = new ArrayList<>();

    public abstract boolean process(CommandSender sender, Command command, String label, String[] args);

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