package net.sparkzz.command.sub;

import net.sparkzz.command.SubCommand;
import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
import net.sparkzz.util.Notifier;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

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
        Store store = InventoryManagementSystem.locateCurrentShop(player);
        setAttribute("store", store.getName());
        if (args.length >= 2) setAttribute("material", args[1]);

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
                case "shop-name" -> store.setName(args[2]);
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
}