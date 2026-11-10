package net.sparkzz.shops.command.sub;

import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import net.sparkzz.shops.util.ShopAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;

/**
 * Browse subcommand used for browsing items to a shop
 *
 * @author Brendon Butler
 */
public class BrowseCommand extends SubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        resetAttributes();
        setArgsAsAttributes(args);
        Player player = (Player) setAttribute("sender", sender);
        Store store = setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));

        if (store == null) {
            Notifier.process(player, NO_STORE_FOUND, getAttributes());
            return true;
        }

        int pageNumber = (args.length > 1) ? Integer.parseInt(args[1]) : 1;

        if (store.getItems().isEmpty()) {
            Notifier.process(sender, STORE_NO_ITEMS, getAttributes());
            return true;
        }

        String page = Notifier.Paginator.buildBrowsePage(store, pageNumber);

        if (page == null) {
            Notifier.process(sender, INVALID_PAGE_NUM, getAttributes());
            return true;
        }

        sender.sendMessage(page);
        return true;
    }

    /**
     * The getAction method is used to identify the action that the subcommand is performing for permissions
     *
     * @return the ShopAction being performed
     */
    @Override
    public ShopAction getAction() {
        return ShopAction.BROWSE;
    }
}