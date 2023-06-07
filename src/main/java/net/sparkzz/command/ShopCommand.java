package net.sparkzz.command;

import net.sparkzz.shops.Shops;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.RED;

/**
 * Shop Command for browsing/buying/selling in the current store
 *
 * @author Brendon Butler
 */
public class ShopCommand extends CommandManager {

    private final AddSubCommand addCmd = new AddSubCommand();
    private final BuySubCommand buyCmd = new BuySubCommand();
    private final SellSubCommand sellCmd = new SellSubCommand();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.format("%sOnly players can use this command!", RED));
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("add", "buy", "sell");
        }

        if (args.length == 2) {
            Set<Material> shopItems = Shops.shop.getItems().keySet();

            // Add command autocomplete item list
            if (args[0].equalsIgnoreCase("add"))
                return Arrays.stream(Material.values())
                        .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());

            // Buy command autocomplete item list
            if (args[0].equalsIgnoreCase("buy"))
                return Arrays.stream(shopItems.toArray())
                        .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());

            // Sell command autocomplete item list
            if (args[0].equalsIgnoreCase("sell"))
                return Arrays.stream(((Player) sender).getInventory().getContents())
                        .filter(Objects::nonNull).map(i -> i.getType().toString().toLowerCase())
                        .collect(Collectors.toList());
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("sell"))) {
            return Arrays.asList("[<quantity>]");
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("add"))) {
            return Arrays.asList("<customer-buy-price>");
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("add"))) {
            return Arrays.asList("<customer-sell-price>");
        }

        if (args.length == 5 && (args[0].equalsIgnoreCase("add"))) {
            return Arrays.asList("<max-quantity>");
        }

        if (args.length == 6 && (args[0].equalsIgnoreCase("add"))) {
            return Arrays.asList("[<quantity>]");
        }

        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (args.length >= 2) {
                // Add command process
                if (args[0].equalsIgnoreCase("add"))
                    return addCmd.process(sender, command, label, args);

                // Buy command process
                if (args[0].equalsIgnoreCase("buy"))
                    return buyCmd.process(sender, command, label, args);

                // Sell command process
                if (args[0].equalsIgnoreCase("sell"))
                    return sellCmd.process(sender, command, label, args);
            }
        } catch (NumberFormatException exception) {
            sender.sendMessage(String.format("%sInvalid quantity!", RED));
        }

        return false;
    }
}