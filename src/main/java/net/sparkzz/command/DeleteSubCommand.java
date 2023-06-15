package net.sparkzz.command;

import net.sparkzz.shops.Store;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;

/**
 * Delete subcommand used for deleting a shop
 *
 * @author Brendon Butler
 */
public class DeleteSubCommand implements ISubCommand {

    private List<Store> stores = new ArrayList<>();

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        Optional<Store> foundStore = identifyStore(args[1]);

        if (stores.size() > 1) {
            sender.sendMessage(String.format("%sMultiple shops matched, please specify the shop's UUID!", RED));
            return true;
        }

        if (foundStore.isEmpty()) {
            sender.sendMessage(String.format("%sCould not find a store with the name and/or UUID of: %s%s%s!", RED, GOLD, args[1], RED));
            return true;
        }

        // TODO: determine a way to check if a player can remove all items from the shop, if they can, remove them all
        // TODO: add force flags (-f will ignore all inventory, then process) (-F will ignore all inventory and finances, then process)

        Store store = foundStore.get();

        String name = store.getName();
        boolean success = Store.STORES.remove(store);

        if (success)
            sender.sendMessage(String.format("%sYou have successfully deleted %s%s%s!", GREEN, GOLD, name, GREEN));
        else sender.sendMessage(String.format("%sSomething went wrong when attempting to delete the shop!", RED));
        return true;
    }

    private Optional<Store> identifyStore(String nameOrUUID) {
        Optional<Store> store = Optional.empty();

        if (nameOrUUID.contains("~")) {
            String[] input = nameOrUUID.split("~");

            stores = Store.STORES.stream().filter(s -> s.getName().equalsIgnoreCase(input[0]) && s.getUUID().toString().equalsIgnoreCase(input[1])).collect(Collectors.toCollection(ArrayList::new));

            if (stores.size() == 1)
                store = Optional.of(stores.get(0));
        } else {
            stores = Store.STORES.stream().filter(s -> s.getName().equalsIgnoreCase(nameOrUUID) || s.getUUID().toString().equalsIgnoreCase(nameOrUUID)).collect(Collectors.toCollection(ArrayList::new));

            if (stores.size() == 1)
                store = Optional.of(stores.get(0));
        }

        return store;
    }
}