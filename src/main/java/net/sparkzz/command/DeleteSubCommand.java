package net.sparkzz.command;

import net.sparkzz.shops.Store;
import net.sparkzz.util.Notifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Optional;

import static net.sparkzz.util.Notifier.CipherKey.*;

/**
 * Delete subcommand used for deleting a shop
 *
 * @author Brendon Butler
 */
public class DeleteSubCommand extends SubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        resetAttributes();
        setArgsAsAttributes(args);
        Optional<Store> foundStore = identifyStore(args[1]);
        setAttribute("sender", sender);
        setAttribute("store", (foundStore.isPresent() ? foundStore.get() : args[1]));

        if (stores.size() > 1) {
            Notifier.process(sender, STORE_MULTI_MATCH, getAttributes());
            return true;
        }

        if (foundStore.isEmpty()) {
            Notifier.process(sender, STORE_NO_STORE_FOUND, getAttributes());
            return true;
        }

        // TODO: determine a way to check if a player can remove all items from the shop, if they can, remove them all
        // TODO: add force flags (-f will ignore all inventory, then process) (-F will ignore all inventory and finances, then process)

        Store store = foundStore.get();

        setAttribute("store", store.getName());
        boolean success = Store.STORES.remove(store);

        if (success)
            Notifier.process(sender, STORE_DELETE_SUCCESS, getAttributes());
        else Notifier.process(sender, STORE_DELETE_FAIL, getAttributes());
        return true;
    }
}