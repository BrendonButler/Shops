package net.sparkzz.command;

import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.ChatColor.*;

/**
 * Buy subcommand used for processing buy transactions
 *
 * @author Brendon Butler
 */
public class UpdateSubCommand implements ISubCommand {

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args)
            throws NumberFormatException {
        Material material = Material.matchMaterial(args[1]);
        Player player = (Player) sender;

        if (args.length == 3) {
            Store store = InventoryManagementSystem.locateCurrentShop(player);

            switch (args[1].toLowerCase()) {
                case "infinite-funds" -> {
                    if (player.hasPermission("shops.update.inf-funds")) {
                        sender.sendMessage(String.format("%sYou do not have permission to set infinite funds in your Shop!", RED));
                        return true;
                    }

                    store.setInfiniteFunds(Boolean.parseBoolean(args[2]));
                }
                case "infinite-stock" -> {
                    if (player.hasPermission("shops.update.inf-stock")) {
                        sender.sendMessage(String.format("%sYou do not have permission to set infinite stock in your Shop!", RED));
                        return true;
                    }

                    store.setInfiniteStock(Boolean.parseBoolean(args[2]));
                }
                case "shop-name" -> store.setName(args[2]);
            }

            sender.sendMessage(String.format("%sYou have successfully updated %s%s%s to %s%s%s in the shop!", GREEN, GOLD, args[1], GREEN, GOLD, args[2], GREEN));

            return true;
        }

        if (args.length < 4)
            return false;

        double value = (args[3].equalsIgnoreCase("true")) ? -1D :
                    (args[3].equalsIgnoreCase("false") ? 0D : Double.parseDouble(args[3]));

        if (material != null) {
            Store store = InventoryManagementSystem.locateCurrentShop(player);
            Map<String, String> inputMapping = new HashMap<>();

            if (!store.containsMaterial(material)) {
                sender.sendMessage(String.format("%sThis material (%s) does not currently exist in the shop!", RED, material));
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
                    sender.sendMessage(String.format("%sYou do not have permission to set infinite stock in your Shop!", RED));
                    return true;
                }

                if (args[3].equalsIgnoreCase("true") && store.getAttributes(material).get("quantity").intValue() > 0) {
                    player.sendMessage(String.format("%sPlease ensure there is no stock in the shop for this item and try again", RED));
                    return true;
                }
            }

            store.getItems().get(material).replace(mapped, (mapped.equals("max_quantity") || mapped.equals("quantity")) ? (int) value : value);

            sender.sendMessage(String.format("%sYou have successfully updated %s%s%s for %s%s%s in the shop to %s%s%s!", GREEN, GOLD, args[2], GREEN, GOLD, material, GREEN, GOLD, args[3], GREEN));
            return true;
        }

        sender.sendMessage(String.format("%sInvalid material (%s)!", RED, args[1]));
        return false;
    }
}