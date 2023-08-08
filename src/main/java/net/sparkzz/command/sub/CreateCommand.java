package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.util.Cuboid;
import net.sparkzz.util.Notifier;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static net.sparkzz.util.Notifier.CipherKey.PLAYER_NOT_FOUND;

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

        OfflinePlayer owner = (Player) sender;
        setAttribute("target", owner);

        if (args.length == 3) {
            if (!sender.hasPermission("shops.create.other-player")) {
                Notifier.process(sender, Notifier.CipherKey.NO_PERMS_CREATE_OTHER, getAttributes());
                return true;
            }

            setAttribute("target", args[2]);

            boolean isUUID = args[2].matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
            Server server = (!Shops.isTest()) ? Shops.getPlugin(Shops.class).getServer() : Shops.getMockServer();
            owner = (!isUUID) ? server.getPlayer(args[2]) : server.getOfflinePlayer(UUID.fromString(args[2]));
        }

        if (owner == null) {
            Notifier.process(sender, PLAYER_NOT_FOUND, getAttributes());
            return true;
        }

        Cuboid cuboid = new Cuboid(
                ((Player) sender).getWorld(),
                ((Player) sender).getLocation().getX() - 20,
                ((Player) sender).getLocation().getY() - 20,
                ((Player) sender).getLocation().getZ() - 20,
                ((Player) sender).getLocation().getX() + 20,
                ((Player) sender).getLocation().getY() + 20,
                ((Player) sender).getLocation().getZ() + 20
        );

        Store store = new Store(args[1], owner.getUniqueId(), cuboid);

        setAttribute("store", store.getName());
        if (owner.getUniqueId().equals(((Player) sender).getUniqueId()))
            Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_SUCCESS, getAttributes());
        else Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_SUCCESS_OTHER_PLAYER, getAttributes());
        return true;
    }
}