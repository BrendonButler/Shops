package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
import net.sparkzz.util.Notifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.stream.Collectors;

import static net.sparkzz.util.Notifier.CipherKey.*;

/**
 * Delete subcommand used for deleting a shop
 *
 * @author Brendon Butler
 */
public class DeleteCommand extends SubCommand {

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

        boolean ignoreInv = false, ignoreFunds = false;

        // TODO: determine a way to check if a player can remove all items from the shop, if they can, remove them all
        // TODO: add force flags (-f will ignore all inventory, then process) (-F will ignore all inventory and finances, then process)
        if (args.length == 3) {
            switch (args[2]) {
                // soft force delete
                case "-f" -> ignoreInv = true;
                // hard force delete
                case "-F" -> {
                    ignoreInv = true;
                    ignoreFunds = true;
                }
                default -> {}
            }
        }

        boolean canInsertAll = false;
        Store store = foundStore.get();
        Player player = (Player) sender;

        setAttribute("store", store.getName());

        if (!ignoreInv)
            canInsertAll = InventoryManagementSystem.canInsertAll(player, store.getItems().entrySet().stream()
                    .map(entry -> new ItemStack(entry.getKey(), (int) entry.getValue().getOrDefault("quantity", 0)))
                    .collect(Collectors.toList()));

        if (!ignoreInv && !canInsertAll) {
            Notifier.process(sender, STORE_DELETE_INSUFFICIENT_INV_PLAYER, getAttributes());
            return true;
        }

        if (!ignoreFunds) {
            Shops.getEconomy().depositPlayer(player, store.getBalance());
            store.setBalance(0);
        }

        boolean success = Store.STORES.remove(store);

        if (success)
            Notifier.process(sender, STORE_DELETE_SUCCESS, getAttributes());
        else Notifier.process(sender, STORE_DELETE_FAIL, getAttributes());
        return true;
    }
}