package net.sparkzz.command;

import net.sparkzz.command.sub.*;
import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.util.InventoryManagementSystem;
import net.sparkzz.util.Notifier;
import net.sparkzz.util.Notifier.CipherKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.RED;

/**
 * Shop Command for browsing/buying/selling/updating items in the current store
 *
 * @author Brendon Butler
 */
public class ShopCommand extends CommandManager {

    private final Map<String, SubCommand> subCommands = new HashMap<>() {{
        put("add", new AddCommand());
        put("browse", new BrowseCommand());
        put("buy", new BuyCommand());
        put("create", new CreateCommand());
        put("delete", new DeleteCommand());
        put("deposit", new DepositCommand());
        put("sell", new SellCommand());
        put("transfer", new TransferCommand());
        put("remove", new RemoveCommand());
        put("update", new UpdateCommand());
        put("withdraw", new WithdrawCommand());
    }};

    /**
     * TabCompleter for generating suggestions when a player starts typing the /shop command
     *
     * @param sender the sender attempting the command
     * @param command the command to be processed
     * @param label the command label
     * @param args the arguments following the command
     * @return a list of options for the /shop command arguments
     */
    // TODO: clean up this mess (reconfigure if-blocks/switches to be more efficient and less clunky
    @Override
    @SuppressWarnings("all")
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.format("%sOnly players can use this command!", RED));
            return new ArrayList<>();
        }

        if (args.length == 1)
            return subCommands.keySet().stream().toList();

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("browse"))
                return Collections.singletonList("<page-number>");

            if (args[0].equalsIgnoreCase("deposit"))
                return Collections.singletonList("<amount>");

            if (args[0].equalsIgnoreCase("withdraw"))
                return List.of("<amount>", "all");

            // Add command autocomplete item list
            if (args[0].equalsIgnoreCase("add")) {
                return Arrays.stream(Material.values())
                        .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());
            }

            Optional<Store> currentStore = InventoryManagementSystem.locateCurrentStore(((Player) sender));
            Set<Material> shopItems = (currentStore.isPresent() ? currentStore.get().getItems().keySet() : Collections.emptySet());

            // Buy/Remove command autocomplete item list
            if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("remove"))
                return shopItems.stream().map(m -> m.toString().toLowerCase()).collect(Collectors.toList());

            // provide a list of items witin the shop, along with some additional items based on permissions
            if (args[0].equalsIgnoreCase("update")) {
                ArrayList<String> tempList = shopItems.stream().map(m -> m.toString().toLowerCase()).collect(Collectors.toCollection(ArrayList::new));
                if (((Player) sender).hasPermission("shops.update.inf-funds")) tempList.add("infinite-funds");
                if (((Player) sender).hasPermission("shops.update.inf-stock")) tempList.add("infinite-stock");
                tempList.add("shop-name");

                return tempList;
            }

            // Sell command autocomplete item list
            if (args[0].equalsIgnoreCase("sell"))
                return Arrays.stream(((Player) sender).getInventory().getContents())
                        .filter(Objects::nonNull).map(i -> i.getType().toString().toLowerCase())
                        .collect(Collectors.toList());

            if (args[0].equalsIgnoreCase("create"))
                return Collections.singletonList("<name>");

            // Only display a list of shops that are owned by the player, and provide a list of "ShopName~UUID"
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("transfer"))
                return Store.STORES.stream().filter(s -> s.getOwner().equals(((Player) sender).getUniqueId())).map(s -> String.format("%s~%s", s.getName(), s.getUUID())).collect(Collectors.toCollection(ArrayList::new));
        }

        Server server = (Shops.isTest() ? Shops.getMockServer() : Shops.getPlugin(Shops.class).getServer());

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("sell"))
                return List.of("[<quantity>]", "all");

            if (args[0].equalsIgnoreCase("add"))
                return List.of("<customer-buy-price>", "[<quantity>]", "all");

            if (args[0].equalsIgnoreCase("buy"))
                return Collections.singletonList("[<quantity>]");

            if (args[0].equalsIgnoreCase("update")) {
                List<String> options;

                switch (args[1].toLowerCase()) {
                    case "infinite-funds", "infinite-stock" -> options = List.of("true", "false");
                    case "shop-name" -> options = Collections.singletonList("<name>");
                    case "location" -> {
                        options = Store.STORES.stream().filter(s -> s.getOwner().equals(((Player) sender).getUniqueId())).map(s -> String.format("%s~%s", s.getName(), s.getUUID())).collect(Collectors.toCollection(ArrayList::new));
                        options.addAll(Bukkit.getWorlds().stream().map(WorldInfo::getName).toList());
                    }
                    default -> options = List.of("customer-buy-price", "customer-sell-price", "infinite-quantity", "max-quantity");
                };

                return options;
            }

            if (args[0].equalsIgnoreCase("transfer"))
                return server.getOnlinePlayers().stream().map(p -> p.getName()).collect(Collectors.toList());

            if (args[0].equalsIgnoreCase("create")) {
                List<String> options = server.getOnlinePlayers().stream().map(p -> p.getName()).collect(Collectors.toList());
                options.add("<x1>");

                return options;
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("add")) {
                return Collections.singletonList("<customer-sell-price>");
            }

            if (args[0].equalsIgnoreCase("update")) {
                if (args[1].equalsIgnoreCase("location")) {
                    List<String> stores = Store.STORES.stream().map(Store::getName).collect(Collectors.toList());
                    List<String> options = Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList());

                    boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                    boolean containsStore = stores.stream().anyMatch(s -> s.equalsIgnoreCase(args[2]));

                    if (containsWorld)
                        options = Collections.singletonList("x1");
                    else if (!containsWorld && !containsStore)
                        options = Collections.singletonList("y1");
                    else if (!containsStore)
                        options.add("x1");

                    return options;
                }

                return switch (args[2].toLowerCase()) {
                    case "infinite-quantity" -> List.of("true", "false");
                    default -> Collections.singletonList("<value>");
                };
            }

            if (args[0].equalsIgnoreCase("create") && server.getPlayer(args[2]) != null)
                return Collections.singletonList("<x1>");
            else if (args[0].equalsIgnoreCase("create"))
                return Collections.singletonList("<y1>");
        }

        if (args.length == 5 ) {
            if (args[0].equalsIgnoreCase("add")) {
                return Collections.singletonList("<max-quantity>");
            }

            if (args[0].equalsIgnoreCase("create") && server.getPlayer(args[2]) != null)
                return Collections.singletonList("<y1>");
            else if (args[0].equalsIgnoreCase("create"))
                return Collections.singletonList("<z1>");

            if (args[0].equalsIgnoreCase("update") && args[1].equalsIgnoreCase("location")) {
                List<String> stores = Store.STORES.stream().map(Store::getName).collect(Collectors.toList());
                List<String> options = Collections.singletonList("x1");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = stores.stream().anyMatch(s -> s.equalsIgnoreCase(args[2]));

                if (containsWorld && !containsStore)
                    options = Collections.singletonList("y1");
                else if (!containsWorld && !containsStore)
                    options = Collections.singletonList("z1");

                return options;
            }
        }

        if (args.length == 6) {
            if (args[0].equalsIgnoreCase("add")) {
                return List.of("[<quantity>]", "all");
            }

            if (args[0].equalsIgnoreCase("create") && server.getPlayer(args[2]) != null)
                return Collections.singletonList("<z1>");
            else if (args[0].equalsIgnoreCase("create"))
                return Collections.singletonList("<x2>");

            if (args[0].equalsIgnoreCase("update") && args[1].equalsIgnoreCase("location")) {
                List<String> stores = Store.STORES.stream().map(Store::getName).collect(Collectors.toList());
                List<String> options = Collections.singletonList("y1");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = stores.stream().anyMatch(s -> s.equalsIgnoreCase(args[2]));

                if (containsWorld && !containsStore)
                    options = Collections.singletonList("z1");
                else if (!containsWorld && !containsStore)
                    options = Collections.singletonList("x2");

                return options;
            }
        }

        if (args.length == 7) {
            if (args[0].equalsIgnoreCase("create") && server.getPlayer(args[2]) != null)
                return Collections.singletonList("<x2>");
            else if (args.length == 7 && args[0].equalsIgnoreCase("create"))
                return Collections.singletonList("<y2>");

            if (args[0].equalsIgnoreCase("update") && args[1].equalsIgnoreCase("location")) {
                List<String> stores = Store.STORES.stream().map(Store::getName).collect(Collectors.toList());
                List<String> options = Collections.singletonList("z1");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = stores.stream().anyMatch(s -> s.equalsIgnoreCase(args[2]));

                if (containsWorld && !containsStore)
                    options = Collections.singletonList("x2");
                else if (!containsWorld && !containsStore)
                    options = Collections.singletonList("y2");

                return options;
            }
        }

        if (args.length == 8) {
            if (args[0].equalsIgnoreCase("create") && server.getPlayer(args[2]) != null)
                return Collections.singletonList("<y2>");
            else if (args[0].equalsIgnoreCase("create"))
                return Collections.singletonList("<z2>");

            if (args[0].equalsIgnoreCase("update") && args[1].equalsIgnoreCase("location")) {
                List<String> stores = Store.STORES.stream().map(Store::getName).collect(Collectors.toList());
                List<String> options = Collections.singletonList("x2");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = stores.stream().anyMatch(s -> s.equalsIgnoreCase(args[2]));

                if (containsWorld && !containsStore)
                    options = Collections.singletonList("y2");
                else if (!containsWorld && !containsStore)
                    options = Collections.singletonList("z2");

                return options;
            }
        }

        if (args.length == 9) {
            if (args[0].equalsIgnoreCase("create") && server.getPlayer(args[2]) != null)
                return Collections.singletonList("<z2>");

            if (args[0].equalsIgnoreCase("update") && args[1].equalsIgnoreCase("location")) {
                List<String> stores = Store.STORES.stream().map(Store::getName).collect(Collectors.toList());
                List<String> options = Collections.singletonList("y2");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = stores.stream().anyMatch(s -> s.equalsIgnoreCase(args[2]));

                if (containsWorld && !containsStore)
                    options = Collections.singletonList("z2");
                else if (!containsWorld && !containsStore)
                    options = Collections.emptyList();

                return options;
            }
        }

        if (args.length == 10 && args[0].equalsIgnoreCase("update") && args[1].equalsIgnoreCase("location")) {
            List<String> stores = Store.STORES.stream().map(Store::getName).collect(Collectors.toList());
            List<String> options = Collections.singletonList("z2");

            boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
            boolean containsStore = stores.stream().anyMatch(s -> s.equalsIgnoreCase(args[2]));

            if ((containsWorld && !containsStore) || (!containsWorld && !containsStore))
                options = Collections.emptyList();

            return options;
        }

        return new ArrayList<>();
    }

    /**
     * The base command for all shop user subcommands
     *
     * @param sender the sender attempting the command
     * @param command the command to be processed
     * @param label the command label
     * @param args the arguments following the command
     * @return whether the command was successful
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        resetAttributes();
        setAttribute("sender", sender);
        setArgsAsAttributes(args);

        if (!(sender instanceof Player)) {
            Notifier.process(sender, CipherKey.ONLY_PLAYERS_CMD, getAttributes());
            return true;
        }

        try {
            if (args.length == 0 || (args.length < 2 && !(args[0].equalsIgnoreCase("browse") || args[0].equalsIgnoreCase("update")))) throw new IllegalArgumentException();

            String subCommand = args[0].toLowerCase();

            if (!sender.hasPermission(String.format("shops.cmd.%s", subCommand))) {
                Notifier.process(sender, CipherKey.NO_PERMS_CMD, getAttributes());
                return true;
            }

            if (subCommands.containsKey(subCommand))
                return subCommands.get(subCommand).process(sender, command, label, args);
        } catch (NumberFormatException exception) {
            sender.sendMessage(String.format("%sInvalid numerical value (%s)!", RED, exception.getMessage().subSequence(exception.getMessage().indexOf("\"") + 1, exception.getMessage().length() - 1)));
        } catch (IllegalArgumentException exception) {
            Notifier.process(sender, CipherKey.INVALID_ARG_CNT, getAttributes());
        }

        // send the CommandSender a usage message based on the subcommand instead of the default
        if (command.getName().equalsIgnoreCase("shop") && args.length > 0)
            return Notifier.usageSubCommand(sender, args);

        return false;
    }
}