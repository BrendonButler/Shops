package net.sparkzz.shops.command;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.sub.AddCommand;
import net.sparkzz.shops.command.sub.CreateCommand;
import net.sparkzz.shops.command.sub.DeleteCommand;
import net.sparkzz.shops.command.sub.RemoveCommand;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifiable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Nameable;

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

/**
 * Shop Command for browsing/buying/selling/updating items in the current store
 *
 * @author Brendon Butler
 */
public class ShopCommand extends Notifiable implements CommandExecutor {

    private static final Map<Iterable<String>, Command.Parameterized> subCommands = new HashMap<>() {{
        put(Collections.singletonList("add"), AddCommand.build());
//        put(Collections.singletonList("browse"), BrowseCommand.build());
//        put(Collections.singletonList("buy"), BuyCommand.build());
        put(Collections.singletonList("create"), CreateCommand.build());
        put(Collections.singletonList("delete"), DeleteCommand.build());
//        put(Collections.singletonList("deposit"), DepositCommand.build());
//        put(Collections.singletonList("sell"), SellCommand.build());
//        put(Collections.singletonList("transfer"), TransferCommand.build());
        put(Collections.singletonList("remove"), RemoveCommand.build());
//        put(Collections.singletonList("update"), UpdateCommand.build());
//        put(Collections.singletonList("withdraw"), WithdrawCommand.build());
    }};

    private List<String> handleSecondArgs(CommandCause sender, String arg0) {
        Optional<Store> currentStore = InventoryManagementSystem.locateCurrentStore(((ServerPlayer) sender));
        Set<ItemType> shopItems = (currentStore.map(store -> store.getItems().keySet()).orElse(Collections.emptySet()));
        ServerPlayer player = (ServerPlayer) sender;

        return switch (arg0) {
            case "add" -> {
                ItemStack[] inventoryContents = player.inventory().slots().stream().map(Inventory::peek).toArray(ItemStack[]::new);

                yield Arrays.stream(inventoryContents)
                        .filter(Objects::nonNull)
                        .filter(m -> player.hasPermission("shops.cmd.add"))
                        .map(item -> item.type().toString().toLowerCase())
                        .toList();
            }
            case "browse" -> (player.hasPermission("shops.cmd.browse") ? Collections.singletonList("<page-number>") : new ArrayList<String>());
            case "buy", "remove" -> shopItems.stream()
                    .filter(s -> (player.hasPermission("shops.cmd." + arg0)))
                    .map(m -> m.toString().toLowerCase()).collect(Collectors.toList());
            case "create" -> (player.hasPermission("shops.cmd.create") ? Collections.singletonList("<name>") : new ArrayList<String>());
            case "delete", "transfer" -> Store.STORES.stream()
                    .filter(s -> (player.hasPermission("shops.cmd." + arg0)) &&
                                 s.getOwner().equals(player.uniqueId()))
                    .map(s -> String.format("%s~%s", s.getName(), s.getUUID()))
                    .toList();
            case "deposit" -> (player.hasPermission("shops.cmd.deposit") ? Collections.singletonList("<amount>") : new ArrayList<String>());
            case "withdraw" -> (player.hasPermission("shops.cmd.withdraw") ? List.of("<amount>", "all") : new ArrayList<String>());
            case "update" -> {
                if (!player.hasPermission("shops.cmd.update"))
                    yield Collections.emptyList();

                ArrayList<String> tempList = shopItems.stream()
                        .filter(m -> player.hasPermission("shops.cmd.update"))
                        .map(m -> m.toString().toLowerCase())
                        .collect(Collectors.toCollection(ArrayList::new));
                if (player.hasPermission("shops.update.inf-funds")) tempList.add("infinite-funds");
                if (player.hasPermission("shops.update.inf-stock")) tempList.add("infinite-stock");
                if (player.hasPermission("shops.update.location")) tempList.add("location");
                tempList.add("store-name");

                yield tempList;
            }
            case "sell" -> {
                ItemStack[] inventoryContents = player.inventory().slots().stream().map(Inventory::peek).toArray(ItemStack[]::new);

                yield Arrays.stream(inventoryContents)
                        .filter(Objects::nonNull)
                        .filter(m -> player.hasPermission("shops.cmd.sell"))
                        .map(item -> item.type().toString().toLowerCase())
                        .toList();
            }
            default -> Collections.emptyList();
        };
    }

    private List<String> handleThirdArgs(CommandCause sender, String[] args) {
        ServerPlayer player = (ServerPlayer) sender;
        
        return switch (args[0]) {
            case "add" -> (player.hasPermission("shops.cmd.add") ? List.of("<customer-buy-price>", "[<quantity>]", "all") : new ArrayList<String>());
            case "buy" -> (player.hasPermission("shops.cmd.buy") ? Collections.singletonList("[<quantity>]") : new ArrayList<String>());
            case "create" -> {
                List<String> options = Sponge.server().onlinePlayers().stream().map(Nameable::name).collect(Collectors.toList());
                options.add("<x1>");

                yield options;
            }
            case "remove", "sell" -> (player.hasPermission("shops.cmd." + args[0]) ? List.of("[<quantity>]", "all") : new ArrayList<String>());
            case "transfer" -> (player.hasPermission("shops.cmd.transfer") ? Sponge.server().onlinePlayers().stream().map(Nameable::name).toList() : new ArrayList<String>());
            case "update" -> {
                List<String> options = new ArrayList<>();

                if (!player.hasPermission("shops.cmd.update"))
                    yield options;

                if ((player.hasPermission("shops.update.inf-funds") && args[1].equals("infinite-funds")) ||
                    (player.hasPermission("shops.update.inf-stock")) && args[1].equals("infinite-stock")) {
                    options = List.of("true", "false");
                } else if (args[1].equals("store-name")) {
                    options = Collections.singletonList("<name>");
                } else if (args[1].equals("location")) {
                    yield Stream.concat(Store.STORES.stream()
                                    .filter(s -> s.getOwner().equals(player.uniqueId()))
                                    .map(s -> String.format("%s~%s", s.getName(), s.getUUID())),
                            Sponge.server().worldManager().worlds().stream().map(w -> w.key().asString())
                    ).toList();
                } else {
                    options = List.of("customer-buy-price", "customer-sell-price", "infinite-quantity", "max-quantity");
                }

                yield options;
            }
            default -> Collections.emptyList();
        };
    }

    private List<String> handleFourthArgs(CommandCause sender, String[] args) {
        ServerPlayer player = (ServerPlayer) sender;

        return switch (args[0]) {
            case "add" -> (player.hasPermission("shops.cmd.add") ? Collections.singletonList("<customer-sell-price>") : new ArrayList<String>());
            case "create" -> {
                if (!player.hasPermission("shops.cmd.create"))
                    yield Collections.emptyList();

                yield (Sponge.server().player(args[2]).isPresent()) ? Collections.singletonList("<x1>") : Collections.singletonList("<y1>");
            }
            case "update" -> {
                if (args[1].equals("location")) {
                    if (!player.hasPermission("shops.cmd.update") || !player.hasPermission("shops.update.location"))
                        yield Collections.emptyList();

                    List<String> options = Sponge.server().worldManager().worlds().stream().map(w -> w.key().asString()).collect(Collectors.toList());

                    boolean containsWorld = Sponge.server().worldManager().worlds().stream().map(w -> w.key().asString()).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
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

    private List<String> handleFifthArgs(CommandCause sender, String[] args) {
        ServerPlayer player = (ServerPlayer) sender;

        return switch (args[0]) {
            case "add" -> (player.hasPermission("shops.cmd.add") ? Collections.singletonList("<max-quantity>") : new ArrayList<String>());
            case "create" -> {
                if (!player.hasPermission("shops.cmd.create"))
                    yield Collections.emptyList();

                yield (Sponge.server().player(args[2]).isPresent()) ? Collections.singletonList("<y1>") : Collections.singletonList("<z1>");
            }
            case "update" -> {
                if (!player.hasPermission("shops.cmd.update") || !player.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<x1>");

                boolean containsWorld = Sponge.server().worldManager().worlds().stream().map(w -> w.key().asString()).anyMatch(w ->
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

    private List<String> handleSixthArgs(CommandCause sender, String[] args) {
        ServerPlayer player = (ServerPlayer) sender;

        return switch (args[0]) {
            case "add" -> (player.hasPermission("shops.cmd.add") ? List.of("[<quantity>]", "all") : new ArrayList<String>());
            case "create" -> {
                if (!player.hasPermission("shops.cmd.create"))
                    yield Collections.emptyList();

                yield (Sponge.server().player(args[2]).isPresent()) ? Collections.singletonList("<z1>") : Collections.singletonList("<x2>");
            }
            case "update" -> {
                if (!player.hasPermission("shops.cmd.update") || !player.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<y1>");

                boolean containsWorld = Sponge.server().worldManager().worlds().stream().map(w -> w.key().asString()).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
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

    private List<String> handleSeventhArgs(CommandCause sender, String[] args) {
        ServerPlayer player = (ServerPlayer) sender;

        return switch (args[0]) {
            case "create" -> {
                if (!player.hasPermission("shops.cmd.create"))
                    yield Collections.emptyList();

                yield (Sponge.server().player(args[2]).isPresent()) ? Collections.singletonList("<x2>") : Collections.singletonList("<y2>");
            }
            case "update" -> {
                if (!player.hasPermission("shops.cmd.update") || !player.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<z1>");

                boolean containsWorld = Sponge.server().worldManager().worlds().stream().map(w -> w.key().asString()).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
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

    private List<String> handleEighthArgs(CommandCause sender, String[] args) {
        ServerPlayer player = (ServerPlayer) sender;

        return switch (args[0]) {
            case "create" -> {
                if (!player.hasPermission("shops.cmd.create"))
                    yield Collections.emptyList();

                yield (Sponge.server().player(args[2]).isPresent()) ? Collections.singletonList("<y2>") : Collections.singletonList("<z2>");
            }
            case "update" -> {
                if (!player.hasPermission("shops.cmd.update") || !player.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<x2>");

                boolean containsWorld = Sponge.server().worldManager().worlds().stream().map(w -> w.key().asString()).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
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

    private List<String> handleNinthArgs(CommandCause sender, String[] args) {
        ServerPlayer player = (ServerPlayer) sender;

        return switch (args[0]) {
            case "create" -> (player.hasPermission("shops.cmd.create") && Sponge.server().player(args[2]).isPresent())
                             ? Collections.singletonList("<z2>") : Collections.emptyList();
            case "update" -> {
                if (!player.hasPermission("shops.cmd.update") || !player.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<y2>");

                boolean containsWorld = Sponge.server().worldManager().worlds().stream().map(w -> w.key().asString()).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
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
    @SuppressWarnings("all")
    public List<String> onTabComplete(@NotNull CommandCause sender, @NotNull Command command, @NotNull String label, String[] args) {
        ServerPlayer player = (ServerPlayer) sender;

        return switch (args.length) {
            case 1 -> {
                List<String> subCommandsFiltered = subCommands.keySet().stream()
                        .map(s -> s.iterator().next())
                        .filter(s -> {
                            return player.hasPermission(String.format("shops.cmd.%s", s));
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
                if (!player.hasPermission("shops.cmd.update") || !player.hasPermission("shops.update.location"))
                    yield Collections.emptyList();

                List<String> options = Collections.singletonList("<z2>");

                boolean containsWorld = Sponge.server().worldManager().worlds().stream().map(w -> w.key().asString()).anyMatch(w -> w.equalsIgnoreCase(args[2]) || w.equalsIgnoreCase(args[3]));
                boolean containsStore = Store.identifyStore(args[2]).isPresent();

                yield ((containsWorld && !containsStore) || (!containsWorld && !containsStore)) ? Collections.emptyList() : options;
            }
            default -> Collections.emptyList();
        };
    }

    /**
     * The base command for all shop user subcommands
     *
     * @param context the CommandContext
     * @return the command result
     */
    public CommandResult execute(CommandContext context) {
        List<Parameter> parameters = (context.executedCommand().isPresent() && !context.executedCommand().get().parameters().isEmpty()) ? context.executedCommand().get().parameters() : Collections.emptyList();
        resetAttributes();
        setAttribute("sender", context.cause());
        setArgsAsAttributes(parameters.stream().map(Object::toString).toArray(String[]::new));

        return CommandResult.success();
    }

    /**
     * Build the Command structure to be registered
     */
    public static Command.Parameterized build() {
        return Command.builder()
                .executor(new ShopCommand())
                .permission("shops.cmd.shops")
                .shortDescription(Component.text("Interact with current shop"))
                .extendedDescription(Component.text("Browse inventory, buy and sell items, and manage your shop"))
                .addChildren(subCommands)
                .build();
    }
}