package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
import net.sparkzz.util.Notifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.sparkzz.util.Notifier.CipherKey.INVALID_PAGE_NUM;
import static net.sparkzz.util.Notifier.CipherKey.STORE_NOT_FOUND;

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
        Store store = (Store) setAttribute("store", InventoryManagementSystem.locateCurrentShop(player));

        int pageNumber = Integer.parseInt(args[1]);

        if (store != null) {
            String page = Notifier.Paginator.buildBrowsePage(store, pageNumber);

            if (page == null) {
                Notifier.process(sender, INVALID_PAGE_NUM, getAttributes());
                return true;
            }

            sender.sendMessage(page);
            return true;
        }

        Notifier.process(sender, STORE_NOT_FOUND, getAttributes());
        return true;
    }
}