package net.sparkzz.shops.command.sub;

import net.sparkzz.shops.AbstractStore;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.Config;
import net.sparkzz.shops.util.Cuboid;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.DoubleStream;

import static net.sparkzz.shops.util.Notifier.CipherKey.PLAYER_NOT_FOUND;

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
        int shopsOwned = 0;

        for (AbstractStore store : Store.STORES)
            if (store.getOwner().equals(((Player) sender).getUniqueId()))
                shopsOwned++;

        if (shopsOwned >= (int) setAttribute("max-stores", Config.getMaxOwnedStores())) {
            Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_FAIL_MAX_STORES, getAttributes());
            return true;
        }

        OfflinePlayer owner = (Player) sender;
        setAttribute("target", owner);

        if (args.length == 3 || args.length == 9) {
            if (!sender.hasPermission("shops.create.other-player")) {
                Notifier.process(sender, Notifier.CipherKey.NO_PERMS_CREATE_OTHER, getAttributes());
                return true;
            }

            setAttribute("target", args[2]);

            boolean isUUID = args[2].matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
            Server server = Shops.getServerInstance();
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
            double minX = (double) setAttribute("min-x", Math.min(x1, x2));
            double maxX = (double) setAttribute("max-x", Math.max(x1, x2));
            double minY = (double) setAttribute("min-y", Math.min(y1, y2));
            double maxY = (double) setAttribute("max-y", Math.max(y1, y2));
            double minZ = (double) setAttribute("min-z", Math.min(z1, z2));
            double maxZ = (double) setAttribute("max-z", Math.max(z1, z2));
            double[] minDims = Config.getMinDimensions();
            double[] maxDims = Config.getMaxDimensions();
            double limitMinX = (double) setAttribute("limit-min-x", minDims[0]);
            double limitMinY = (double) setAttribute("limit-min-y", minDims[1]);
            double limitMinZ = (double) setAttribute("limit-min-z", minDims[2]);
            double limitMaxX = (double) setAttribute("limit-max-x", maxDims[0]);
            double limitMaxY = (double) setAttribute("limit-max-y", maxDims[1]);
            double limitMaxZ = (double) setAttribute("limit-max-z", maxDims[2]);

            if ((maxX - minX) < limitMinX || (maxY - minY) < limitMinY || (maxZ - minZ) < limitMinZ) {
                Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_FAIL_MIN_DIMS, getAttributes());
                return true;
            }

            if ((limitMaxX > 0 && (maxX - minX) > limitMaxX) || (limitMaxY > 0 && (maxY - minY) > limitMaxY) || (limitMaxZ > 0 && (maxZ - minZ) > limitMaxZ)) {
                Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_FAIL_MAX_DIMS, getAttributes());
                return true;
            }

            double volume = (double) setAttribute("volume", (maxX - minX) * (maxY - minY) * (maxZ - minZ));
            double minVolume = (double) setAttribute("limit-min-vol", Config.getMinVolume());
            double maxVolume = (double) setAttribute("limit-max-vol", Config.getMaxVolume());

            if (volume < minVolume) {
                Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_FAIL_MIN_VOL, getAttributes());
                return true;
            }

            if (maxVolume > 0 && volume > maxVolume) {
                Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_FAIL_MAX_VOL, getAttributes());
                return true;
            }

            Cuboid cuboid = new Cuboid(((Player) sender).getWorld(), x1, y1, z1, x2, y2, z2);

            for (Cuboid currentCuboid : Config.getOffLimitsCuboids()) {
                if (cuboid.intersects(currentCuboid) || currentCuboid.intersects(cuboid)) {
                    Notifier.process(sender, Notifier.CipherKey.STORE_CREATE_FAIL_OFFLIMITS, getAttributes());
                    return true;
                }
            }

            for (Cuboid currentCuboid : Store.getStores().stream().map(Store::getCuboidLocation).toList()) {
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