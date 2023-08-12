package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.util.Config;
import net.sparkzz.util.Cuboid;
import net.sparkzz.util.Notifier;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.DoubleStream;

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

        if (args.length == 3 || args.length == 9) {
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

        double x1, y1, z1, x2, y2, z2;
        x1 = y1 = z1 = x2 = y2 = z2 = 0D;

        if (args.length == 8) {
            x1 = Double.parseDouble(args[2]);
            y1 = Double.parseDouble(args[3]);
            z1 = Double.parseDouble(args[4]);
            x2 = Double.parseDouble(args[5]);
            y2 = Double.parseDouble(args[6]);
            z2 = Double.parseDouble(args[7]);
        }

        if (args.length == 9) {
            x1 = Double.parseDouble(args[3]);
            y1 = Double.parseDouble(args[4]);
            z1 = Double.parseDouble(args[5]);
            x2 = Double.parseDouble(args[6]);
            y2 = Double.parseDouble(args[7]);
            z2 = Double.parseDouble(args[8]);
        }

        Store store;

        if (DoubleStream.of(x1, y1, z1, x2, y2, z2).allMatch(value -> value == 0D))
            store = new Store(args[1], owner.getUniqueId());
        else {
            Cuboid cuboid = new Cuboid(((Player) sender).getWorld(), x1, y1, z1, x2, y2, z2);

            for (Cuboid currentCuboid : Config.getOffLimitsCuboids()) {
                if (cuboid.intersects(currentCuboid) || currentCuboid.intersects(cuboid)) {
                    Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_FAIL_OFFLIMITS, getAttributes());
                    return true;
                }
            }

            for (Cuboid currentCuboid : Store.STORES.stream().map(Store::getCuboidLocation).toList()) {
                if (cuboid.intersects(currentCuboid) || currentCuboid.intersects(cuboid)) {
                    Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_FAIL_OVERLAPS, getAttributes());
                    return true;
                }
            }

            store = new Store(args[1], owner.getUniqueId(), cuboid);
        }

        setAttribute("store", store.getName());

        if (owner.getUniqueId().equals(((Player) sender).getUniqueId()))
            Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_SUCCESS, getAttributes());
        else Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_SUCCESS_OTHER_PLAYER, getAttributes());
        return true;
    }
}