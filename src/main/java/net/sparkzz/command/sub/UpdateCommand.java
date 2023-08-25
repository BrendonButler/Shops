package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
import net.sparkzz.shops.Store;
import net.sparkzz.util.Cuboid;
import net.sparkzz.util.InventoryManagementSystem;
import net.sparkzz.util.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.sparkzz.util.Notifier.CipherKey.*;

/**
 * Update subcommand used for updating items in a shop
 *
 * @author Brendon Butler
 */
public class UpdateCommand extends SubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        resetAttributes();
        setArgsAsAttributes(args);
        Player player = (Player) setAttribute("sender", sender);
        Store store = (Store) setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));

        if (args.length >= 8 && args[1].equalsIgnoreCase("location")) {
            if (!player.hasPermission("shops.update.location")) {
                Notifier.process(sender, NO_PERMS_LOCATION, getAttributes());
                return true;
            }

            switch (args.length) {
                case 8 -> {
                    if (store == null) {
                        Notifier.process(player, NO_STORE_FOUND, getAttributes());
                        return true;
                    }

                    World world = Bukkit.getWorld((String) setAttribute("world", store.getCuboidLocation().getWorld().getName()));

                    store.setCuboidLocation(generateCuboid(world, args[2], args[3], args[4], args[5], args[6], args[7]));
                }
                case 9 -> {
                    Optional<Store> foundStore = identifyStore((String) setAttribute("store", args[2]));
                    World world = Bukkit.getWorld((String) setAttribute("world", args[2]));
                    store = (Store) setAttribute("store", foundStore.orElse(store));

                    if (store == null) {
                        Notifier.process(player, NO_STORE_FOUND, getAttributes());
                        return true;
                    }

                    if (foundStore.isEmpty() && world == null) {
                        Notifier.process(sender, Notifier.CipherKey.WORLD_NOT_FOUND, getAttributes());
                        return true;
                    } else if (foundStore.isPresent())
                        world = Bukkit.getWorld((String) setAttribute("world", store.getCuboidLocation().getWorld().getName()));

                    store.setCuboidLocation(generateCuboid(world, args[3], args[4], args[5], args[6], args[7], args[8]));
                }
                case 10 -> {
                    Optional<Store> foundStore = identifyStore((String) setAttribute("store", args[2]));
                    World world = Bukkit.getWorld((String) setAttribute("world", args[3]));

                    if (foundStore.isEmpty()) {
                        Notifier.process(sender, STORE_NO_STORE_FOUND, getAttributes());
                        return true;
                    }

                    store = foundStore.get();
                    setAttribute("store", store.getName());

                    if (world == null) {
                        Notifier.process(sender, Notifier.CipherKey.WORLD_NOT_FOUND, getAttributes());
                        return true;
                    }

                    store.setCuboidLocation(generateCuboid(world, args[4], args[5], args[6], args[7], args[8], args[9]));
                }
                default -> {
                    return false;
                }
            }

            Notifier.process(sender, Notifier.CipherKey.STORE_UPDATE_SUCCESS_LOCATION, getAttributes());
            return true;
        }

        if (args.length >= 2) setAttribute("material", args[1]);

        if (store == null) {
            Notifier.process(player, NO_STORE_FOUND, getAttributes());
            return true;
        }

        if (args.length == 3) {
            switch (args[1].toLowerCase()) {
                case "infinite-funds" -> {
                    if (!player.hasPermission("shops.update.inf-funds")) {
                        Notifier.process(sender, NO_PERMS_INF_FUNDS, getAttributes());
                        return true;
                    }

                    store.setInfiniteFunds(Boolean.parseBoolean(args[2]));
                }
                case "infinite-stock" -> {
                    if (!player.hasPermission("shops.update.inf-stock")) {
                        Notifier.process(sender, NO_PERMS_INF_STOCK, getAttributes());
                        return true;
                    }

                    store.setInfiniteStock(Boolean.parseBoolean(args[2]));
                }
                case "store-name" -> store.setName(args[2]);
                default -> {
                    return false;
                }
            }

            Notifier.process(sender, STORE_UPDATE_SUCCESS, getAttributes());
            return true;
        }

        if (args.length < 4)
            return false;

        Material material = Material.matchMaterial(args[1]);

        double value = (args[3].equalsIgnoreCase("true")) ? -1D :
                    (args[3].equalsIgnoreCase("false") ? 0D : Double.parseDouble(args[3]));

        if (material != null) {
            Map<String, String> inputMapping = new HashMap<>();

            if (!store.containsMaterial(material)) {
                Notifier.process(sender, MATERIAL_MISSING_STORE, getAttributes());
                return true;
            }

            inputMapping.put("customer-buy-price", "buy");
            inputMapping.put("customer-sell-price", "sell");
            inputMapping.put("max-quantity", "max_quantity");
            inputMapping.put("infinite-quantity", "quantity");

            if (!inputMapping.containsKey(args[2])) return false;

            String mapped = inputMapping.get(args[2]);

            if (mapped.equals("quantity")) {
                if (!player.hasPermission("shops.update.inf-stock")) {
                    Notifier.process(sender, NO_PERMS_INF_STOCK, getAttributes());
                    return true;
                }

                if (args[3].equalsIgnoreCase("true") && store.getAttributes(material).get("quantity").intValue() > 0) {
                    Notifier.process(sender, STORE_UPDATE_NO_STOCK, getAttributes());
                    return true;
                }
            }

            store.getItems().get(material).replace(mapped, (mapped.equals("max_quantity") || mapped.equals("quantity")) ? (int) value : value);
            Notifier.process(sender, STORE_UPDATE_SUCCESS_2, getAttributes());
            return true;
        }

        Notifier.process(sender, INVALID_MATERIAL, getAttributes());
        return false;
    }

    private Cuboid generateCuboid(World world, String x1String, String y1String, String z1String, String x2String, String y2String, String z2String) {
        double x1 = (double) setAttribute("x1", Double.parseDouble(x1String));
        double y1 = (double) setAttribute("y1", Double.parseDouble(y1String));
        double z1 = (double) setAttribute("z1", Double.parseDouble(z1String));
        double x2 = (double) setAttribute("x2", Double.parseDouble(x2String));
        double y2 = (double) setAttribute("y2", Double.parseDouble(y2String));
        double z2 = (double) setAttribute("z2", Double.parseDouble(z2String));

        return new Cuboid(world, x1, y1, z1, x2, y2, z2);
    }
}