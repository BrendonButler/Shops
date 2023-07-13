package net.sparkzz.command;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;

/**
 * Transfer subcommand used for transferring a shop from one player to another
 *
 * @author Brendon Butler
 */
public class TransferSubCommand extends ISubCommand {

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

        boolean isUUID = args[2].matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

        Server server = Shops.getPlugin(Shops.class).getServer();
        OfflinePlayer targetPlayer = (!isUUID) ? server.getPlayer(args[2]) : server.getOfflinePlayer(UUID.fromString(args[2]));

        if (targetPlayer == null) {
            sender.sendMessage(String.format("%sPlayer (%s) not found!", RED, args[2]));
            return true;
        }

        Store store = foundStore.get();

        store.setOwner(targetPlayer.getUniqueId());
        sender.sendMessage(String.format("%sYou have successfully transferred %s%s%s to player %s%s%s!", GREEN, GOLD, store.getName(), GREEN, GOLD, (targetPlayer.getName() == null) ? args[2] : targetPlayer.getName(), GREEN));
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