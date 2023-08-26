package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.util.Config;
import net.sparkzz.util.Notifier;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.UUID;

import static net.sparkzz.util.Notifier.CipherKey.*;

/**
 * Transfer subcommand used for transferring a shop from one player to another
 *
 * @author Brendon Butler
 */
public class TransferCommand extends SubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        try {
            resetAttributes();
            setArgsAsAttributes(args);
            setAttribute("sender", sender);
            Optional<Store> foundStore;
                foundStore = identifyStore(args[1]);
            setAttribute("store", (foundStore.isPresent() ? foundStore.get() : args[1]));

            if (foundStore.isEmpty()) {
                Notifier.process(sender, STORE_NO_STORE_FOUND, getAttributes());
                return true;
            }

            boolean isUUID = args[2].matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

            // TODO: remove mock references once Server mocking is updated to fix issues with getServer()
            setAttribute("target", args[2]);
            Server server = (!Shops.isTest()) ? Shops.getPlugin(Shops.class).getServer() : Shops.getMockServer();
            OfflinePlayer targetPlayer = (!isUUID) ? server.getPlayer(args[2]) : server.getOfflinePlayer(UUID.fromString(args[2]));

            if (targetPlayer == null) {
                Notifier.process(sender, PLAYER_NOT_FOUND, getAttributes());
                return true;
            }

            Store store = foundStore.get();

            setAttribute("target", targetPlayer.getName());

            if (!sender.isOp()) {
                int shopsOwned = 0;

                for (Store existingStore : Store.STORES)
                    if (existingStore.getOwner().equals(targetPlayer.getUniqueId())) {
                        shopsOwned++;
                    }

                if (shopsOwned >= (int) setAttribute("max-stores", Config.getMaxOwnedStores())) {
                    Notifier.process(sender, Notifier.CipherKey.STORE_TRANSFER_FAIL_MAX_STORES, getAttributes());
                    return true;
                }
            }

            store.setOwner(targetPlayer.getUniqueId());
            Notifier.process(sender, STORE_TRANSFER_SUCCESS, getAttributes());
        } catch (Shops.MultipleStoresMatchedException exception) {
            Notifier.process(sender, STORE_MULTI_MATCH, getAttributes());
        }
        return true;
    }
}
