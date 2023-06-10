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
    private final RemoveSubCommand removeCmd = new RemoveSubCommand();
    private final UpdateSubCommand updateCmd = new UpdateSubCommand();

    @Override
    @SuppressWarnings("all")
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.format("%sOnly players can use this command!", RED));
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("add", "buy", "sell", "remove", "update");
        }

        if (args.length == 2) {
            Set<Material> shopItems = Shops.shop.getItems().keySet();

            // Add command autocomplete item list
            if (args[0].equalsIgnoreCase("add"))
                return Arrays.stream(Material.values())
                        .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());

            // Buy/Remove command autocomplete item list
            if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("remove"))
                return Arrays.stream(shopItems.toArray())
                        .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());

            if (args[0].equalsIgnoreCase("update")) {
                ArrayList<String> tempList = shopItems.stream().map(m -> m.toString().toLowerCase()).collect(Collectors.toCollection(ArrayList::new));
                tempList.add("infinite-funds");
                tempList.add("infinite-stock");
                tempList.add("shop-name");

                return tempList;
            }

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

        if (args.length == 3 && (args[0].equalsIgnoreCase("update"))) {
            return switch (args[1].toLowerCase()) {
                case "infinite-funds", "infinite-stock" -> Arrays.asList("true", "false");
                case "shop-name" -> Arrays.asList("<name>");
                default -> Arrays.asList("customer-buy-price", "customer-sell-price", "max-quantity");
            };
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("add"))) {
            return Arrays.asList("<customer-sell-price>");
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("update"))) {
            return Arrays.asList("<value>");
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
            if (args.length < 2) throw new NumberFormatException();

            switch (args[0].toLowerCase()) {
                case "add" -> {
                    return addCmd.process(sender, command, label, args);
                }
                case "remove" -> {
                    return removeCmd.process(sender, command, label, args);
                }
                case "update" -> {
                    return updateCmd.process(sender, command, label, args);
                }
                case "buy" -> {
                    return buyCmd.process(sender, command, label, args);
                }
                case "sell" -> {
                    return sellCmd.process(sender, command, label, args);
                }
            }
        } catch (NumberFormatException exception) {
            sender.sendMessage(String.format("%sInvalid number of arguments!", RED));
        }

        return false;
    }
}