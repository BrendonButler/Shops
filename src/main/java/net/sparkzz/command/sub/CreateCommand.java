package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
import net.sparkzz.shops.Store;
import net.sparkzz.util.Notifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Create subcommand used for creating a shop
 *
 * @author Brendon Butler
 */
public class CreateCommand extends SubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        resetAttributes();
        setAttribute("sender", sender);
        setArgsAsAttributes(args);
        // TODO: new permission to limit a player to a number of shops (shops.create.<quantity>)

        Store store = new Store(args[1], ((Player) sender).getUniqueId());

        setAttribute("store", store.getName());
        Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_SUCCESS, getAttributes());
        return true;
    }
}