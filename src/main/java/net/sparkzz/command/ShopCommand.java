package net.sparkzz.command;

import net.sparkzz.shops.Shops;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.RED;

/**
 * Shop Command for browsing/buying/selling/updating items in the current store
 *
 * @author Brendon Butler
 */
public class ShopCommand extends CommandManager {

    private final Map<String, ISubCommand> subCommands = new HashMap<>() {{
        put("add", new AddSubCommand());
        put("buy", new BuySubCommand());
        put("sell", new SellSubCommand());
        put("remove", new RemoveSubCommand());
        put("update", new UpdateSubCommand());
    }};

    @Override
    @SuppressWarnings("all")
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.format("%sOnly players can use this command!", RED));
            return new ArrayList<>();
        }

        if (args.length == 1)
            return subCommands.keySet().stream().toList();

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

        if (args.length == 3 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("sell"))) {
            return Arrays.asList("[<quantity>]", "all");
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("add"))) {
            return Arrays.asList("<customer-buy-price>", "[<quantity>]", "all");
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("buy"))) {
            return Arrays.asList("[<quantity>]");
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("update"))) {
            return switch (args[1].toLowerCase()) {
                case "infinite-funds", "infinite-stock" -> Arrays.asList("true", "false");
                case "shop-name" -> Arrays.asList("<name>");
                default -> Arrays.asList("customer-buy-price", "customer-sell-price", "infinite-quantity", "max-quantity");
            };
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("add"))) {
            return Arrays.asList("<customer-sell-price>");
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("update"))) {
            return switch (args[2].toLowerCase()) {
                case "infinite-quantity" -> Arrays.asList("true", "false");
                default -> Arrays.asList("<value>");
            };
        }

        if (args.length == 5 && (args[0].equalsIgnoreCase("add"))) {
            return Arrays.asList("<max-quantity>");
        }

        if (args.length == 6 && (args[0].equalsIgnoreCase("add"))) {
            return Arrays.asList("[<quantity>]", "all");
        }

        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.format("%sOnly players can use this command!", RED));
            return true;
        }

        try {
            if (args.length < 2) throw new IllegalArgumentException();

            if (subCommands.containsKey(args[0].toLowerCase()))
                return subCommands.get(args[0].toLowerCase()).process(sender, command, label, args);
        } catch (NumberFormatException exception) {
            sender.sendMessage(String.format("%sInvalid numerical value (%s)", RED, exception.getMessage().subSequence(exception.getMessage().indexOf("\"") + 1, exception.getMessage().length() - 1)));
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(String.format("%sInvalid number of arguments!", RED));
        }

        return false;
    }
}