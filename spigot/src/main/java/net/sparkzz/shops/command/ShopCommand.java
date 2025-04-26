package net.sparkzz.shops.command;

import net.sparkzz.shops.Shops;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.sub.*;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
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
import java.util.stream.Stream;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;
import static org.bukkit.ChatColor.RED;

/**
 * Shop Command for browsing/buying/selling/updating items in the current store
 *
 * @author Brendon Butler
 */
public class ShopCommand extends CommandManager {

    private final Server server = Shops.getServerInstance();

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

    private List<String> handleSecondArgs(CommandSender sender, String arg0) {
        Optional<Store> currentStore = InventoryManagementSystem.locateCurrentStore(((Player) sender));
        Set<Material> shopItems = (currentStore.map(store -> store.getItems().keySet()).orElse(Collections.emptySet()));

        return switch (arg0) {
            case "add" -> {
                Player player = (Player) sender;
                ItemStack[] inventoryContents = player.getInventory().getContents();

                yield Arrays.stream(inventoryContents)
                        .filter(Objects::nonNull)
                        .filter(m -> sender.hasPermission("shops.cmd.add"))
                        .map(item -> item.getType().toString().toLowerCase())
                        .toList();
            }
            case "browse" -> (sender.hasPermission("shops.cmd.browse") ? Collections.singletonList("<page-number>") : new ArrayList<String>());
            case "buy", "remove" -> shopItems.stream()
                    .filter(s -> (sender.hasPermission("shops.cmd." + arg0)))
                    .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());
            case "create" -> (sender.hasPermission("shops.cmd.create") ? Collections.singletonList("<name>") : new ArrayList<String>());
            case "delete", "transfer" -> Store.STORES.stream()
                    .filter(s -> (sender.hasPermission("shops.cmd." + arg0)) &&
                                 s.getOwner().equals(((Player) sender).getUniqueId()))
                    .map(s -> String.format("%s~%s", s.getName(), s.getUUID()))
                    .toList();
            case "deposit" -> (sender.hasPermission("shops.cmd.deposit") ? Collections.singletonList("<amount>") : new ArrayList<String>());
            case "withdraw" -> (sender.hasPermission("shops.cmd.withdraw") ? List.of("<amount>", "all") : new ArrayList<String>());
            case "update" -> {
                if (!sender.hasPermission("shops.cmd.update"))
                    yield Collections.emptyList();

                ArrayList<String> tempList = shopItems.stream()
                        .filter(m -> sender.hasPermission("shops.cmd.update"))
                        .map(m -> m.toString().toLowerCase())
                        .collect(Collectors.toCollection(ArrayList::new));
                if (sender.hasPermission("shops.update.inf-funds")) tempList.add("infinite-funds");
                if (sender.hasPermission("shops.update.inf-stock")) tempList.add("infinite-stock");
                if (sender.hasPermission("shops.update.location")) tempList.add("location");
                tempList.add("store-name");

                yield tempList;
            }
            case "sell" -> {
                Player player = (Player) sender;
                ItemStack[] inventoryContents = player.getInventory().getContents();

                yield Arrays.stream(inventoryContents)
                        .filter(Objects::nonNull)
                        .filter(m -> player.hasPermission("shops.cmd.sell"))
                        .map(item -> item.getType().toString().toLowerCase())
                        .toList();
            }
            default -> Collections.emptyList();
        };
    }

    private List<String> handleThirdArgs(CommandSender sender, String[] args) {
        return switch (args[0]) {
            case "add" -> (sender.hasPermission("shops.cmd.add") ? List.of("<customer-buy-price>", "[<quantity>]", "all") : new ArrayList<String>());
            case "buy" -> (sender.hasPermission("shops.cmd.buy") ? Collections.singletonList("[<quantity>]") : new ArrayList<String>());
            case "create" -> {
                List<String> options = server.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
                options.add("<x1>");

                yield options;
            }
            case "remove", "sell" -> (sender.hasPermission("shops.cmd." + args[0]) ? List.of("[<quantity>]", "all") : new ArrayList<String>());
            case "transfer" -> (sender.hasPermission("shops.cmd.transfer") ? server.getOnlinePlayers().stream().map(HumanEntity::getName).toList() : new ArrayList<String>());
            case "update" -> {
                List<String> options = new ArrayList<>();

                if (!sender.hasPermission("shops.cmd.update"))
                    yield options;

                if ((sender.hasPermission("shops.update.inf-funds") && args[1].equals("infinite-funds")) ||
                    (sender.hasPermission("shops.update.inf-stock")) && args[1].equals("infinite-stock")) {
                    options = List.of("true", "false");
                } else if (args[1].equals("store-name")) {
                    options = Collections.singletonList("<name>");
                } else if (args[1].equals("location")) {
                    yield Stream.concat(Store.STORES.stream()
                                    .filter(s -> s.getOwner().equals(((Player) sender).getUniqueId()))
                                    .map(s -> String.format("%s~%s", s.getName(), s.getUUID())),
                            Bukkit.getWorlds().stream().map(WorldInfo::getName)
                    ).toList();
                } else {
                    options = List.of("customer-buy-price", "customer-sell-price", "infinite-quantity", "max-quantity");
                }

                yield options;
            }
            default -> Collections.emptyList();
        };
    }

    private List<String> handleFourthArgs(CommandSender sender, String[] args) {
        return switch (args[0]) {
            case "add" -> (sender.hasPermission("shops.cmd.add") ? Collections.singletonList("<customer-sell-price>") : new ArrayList<String>());
            case "create" -> {
                if (!sender.hasPermission("shops.cmd.create"))
                    yield Collections.emptyList();

                yield (server.getPlayer(args[2]) != null) ? Collections.singletonList("<x1>") : Collections.singletonList("<y1>");
            }
            case "update" -> {
                if (args[1].equals("location")) {
                    if (!sender.hasPermission("shops.cmd.update") || !sender.hasPermission("shops.update.location"))
                        yield Collections.emptyList();

                    List<String> options = Bukkit.getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList());

                    boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                    boolean containsStore = Store.identifyStore(args[2]).isPresent();

                    if (containsWorld)
                        options = Collections.singletonList("<x1>");
                    else if (!containsStore)
                        options = Collections.singletonList("<y1>");
                    else options.add("<x1>");

                    yield options;
                }

                yield (args[2].equals("infinite-quantity") ? List.of("true", "false") : Collections.singletonList("<value>"));
            }
            default -> Collections.emptyList();
        };
    }

    private List<String> handleFifthArgs(CommandSender sender, String[] args) {
        return switch (args[0]) {
            case "add" -> (sender.hasPermission("shops.cmd.add") ? Collections.singletonList("<max-quantity>") : new ArrayList<String>());
            case "create" -> {
                if (!sender.hasPermission("shops.cmd.create"))
                    yield Collections.emptyList();

                yield (server.getPlayer(args[2]) != null) ? Collections.singletonList("<y1>") : Collections.singletonList("<z1>");
            }
            case "update" -> {
                if (!sender.hasPermission("shops.cmd.update") || !sender.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<x1>");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w ->
                        w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = Store.identifyStore(args[2]).isPresent();

                if (containsWorld && !containsStore)
                    options = Collections.singletonList("<y1>");
                else if (!containsWorld && !containsStore)
                    options = Collections.singletonList("<z1>");

                yield options;
            }
            default -> Collections.emptyList();
        };
    }

    private List<String> handleSixthArgs(CommandSender sender, String[] args) {
        return switch (args[0]) {
            case "add" -> (sender.hasPermission("shops.cmd.add") ? List.of("[<quantity>]", "all") : new ArrayList<String>());
            case "create" -> {
                if (!sender.hasPermission("shops.cmd.create"))
                    yield Collections.emptyList();

                yield (server.getPlayer(args[2]) != null) ? Collections.singletonList("<z1>") : Collections.singletonList("<x2>");
            }
            case "update" -> {
                if (!sender.hasPermission("shops.cmd.update") || !sender.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<y1>");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = Store.identifyStore(args[2]).isPresent();

                if (containsWorld && !containsStore)
                    options = Collections.singletonList("<z1>");
                else if (!containsWorld && !containsStore)
                    options = Collections.singletonList("<x2>");

                yield options;
            }
            default -> Collections.emptyList();
        };
    }

    private List<String> handleSeventhArgs(CommandSender sender, String[] args) {
        return switch (args[0]) {
            case "create" -> {
                if (!sender.hasPermission("shops.cmd.create"))
                    yield Collections.emptyList();

                yield (server.getPlayer(args[2]) != null) ? Collections.singletonList("<x2>") : Collections.singletonList("<y2>");
            }
            case "update" -> {
                if (!sender.hasPermission("shops.cmd.update") || !sender.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<z1>");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = Store.identifyStore(args[2]).isPresent();

                if (containsWorld && !containsStore)
                    options = Collections.singletonList("<x2>");
                else if (!containsWorld && !containsStore)
                    options = Collections.singletonList("<y2>");

                yield options;
            }
            default -> Collections.emptyList();
        };
    }

    private List<String> handleEighthArgs(CommandSender sender, String[] args) {
        return switch (args[0]) {
            case "create" -> {
                if (!sender.hasPermission("shops.cmd.create"))
                    yield Collections.emptyList();

                yield (server.getPlayer(args[2]) != null) ? Collections.singletonList("<y2>") : Collections.singletonList("<z2>");
            }
            case "update" -> {
                if (!sender.hasPermission("shops.cmd.update") || !sender.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<x2>");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = Store.identifyStore(args[2]).isPresent();

                if (containsWorld && !containsStore)
                    options = Collections.singletonList("<y2>");
                else if (!containsWorld && !containsStore)
                    options = Collections.singletonList("<z2>");

                yield options;
            }
            default -> Collections.emptyList();
        };
    }

    private List<String> handleNinthArgs(CommandSender sender, String[] args) {
        return switch (args[0]) {
            case "create" -> (sender.hasPermission("shops.cmd.create") && server.getPlayer(args[2]) != null)
                             ? Collections.singletonList("<z2>") : Collections.emptyList();
            case "update" -> {
                if (!sender.hasPermission("shops.cmd.update") || !sender.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<y2>");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = Store.identifyStore(args[2]).isPresent();

                if (containsWorld && !containsStore)
                    options = Collections.singletonList("<z2>");
                else if (!containsWorld && !containsStore)
                    options = Collections.emptyList();

                yield options;
            }
            default -> Collections.emptyList();
        };
    }

    /**
     * TabCompleter for generating suggestions when a player starts typing the /shop command
     *
     * @param sender the sender attempting the command
     * @param command the command to be processed
     * @param label the command label
     * @param args the arguments following the command
     * @return a list of options for the /shop command arguments
     */
    @Override
    @SuppressWarnings("all")
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.format("%sOnly players can use this command!", RED));
            return new ArrayList<>();
        }

        return switch (args.length) {
            case 1 -> {
                List<String> subCommandsFiltered = subCommands.keySet().stream().filter(s -> {
                    return sender.hasPermission(String.format("shops.cmd.%s", s));
                }).collect(Collectors.toList());

                yield subCommandsFiltered;
            }
            case 2 -> handleSecondArgs(sender, args[0].toLowerCase());
            case 3 -> handleThirdArgs(sender, Arrays.stream(args).map(s -> s.toLowerCase()).toArray(String[]::new));
            case 4 -> handleFourthArgs(sender, Arrays.stream(args).map(s -> s.toLowerCase()).toArray(String[]::new));
            case 5 -> handleFifthArgs(sender, Arrays.stream(args).map(s -> s.toLowerCase()).toArray(String[]::new));
            case 6 -> handleSixthArgs(sender, Arrays.stream(args).map(s -> s.toLowerCase()).toArray(String[]::new));
            case 7 -> handleSeventhArgs(sender, Arrays.stream(args).map(s -> s.toLowerCase()).toArray(String[]::new));
            case 8 -> handleEighthArgs(sender, Arrays.stream(args).map(s -> s.toLowerCase()).toArray(String[]::new));
            case 9 -> handleNinthArgs(sender, Arrays.stream(args).map(s -> s.toLowerCase()).toArray(String[]::new));
            case 10 -> {
                if (!sender.hasPermission("shops.cmd.update") || !sender.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<z2>");

                boolean containsWorld = Bukkit.getWorlds().stream().map(WorldInfo::getName).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = Store.identifyStore(args[2]).isPresent();

                yield ((containsWorld && !containsStore) || (!containsWorld && !containsStore)) ? Collections.emptyList() : options;
            }
            default -> Collections.emptyList();
        };
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
            Notifier.process(sender, ONLY_PLAYERS_CMD, getAttributes());
            return true;
        }

        try {
            if (args.length == 0 || (args.length < 2 && !(args[0].equalsIgnoreCase("browse") || args[0].equalsIgnoreCase("update")))) throw new IllegalArgumentException();

            String subCommand = args[0].toLowerCase();

            if (!sender.hasPermission(String.format("shops.cmd.%s", subCommand))) {
                Notifier.process(sender, NO_PERMS_CMD, getAttributes());
                return true;
            }

            if (subCommands.containsKey(subCommand))
                return subCommands.get(subCommand).process(sender, command, label, args);
        } catch (NumberFormatException exception) {
            sender.sendMessage(String.format("%sInvalid numerical value (%s)!", RED, exception.getMessage().subSequence(exception.getMessage().indexOf("\"") + 1, exception.getMessage().length() - 1)));
        } catch (IllegalArgumentException exception) {
            Notifier.process(sender, INVALID_ARG_CNT, getAttributes());
        }

        // send the CommandSender a usage message based on the subcommand instead of the default
        if (command.getName().equalsIgnoreCase("shop") && args.length > 0)
            return Notifier.usageSubCommand(sender, args);

        return false;
    }
}