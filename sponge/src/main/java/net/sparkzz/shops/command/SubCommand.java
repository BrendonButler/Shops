package net.sparkzz.shops.command;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.Core;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifiable;
import net.sparkzz.shops.util.Notifier;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.STORE_MULTI_MATCH;
import static org.spongepowered.api.item.inventory.query.QueryTypes.ITEM_TYPE;

/**
 * Interface for sub command layout
 *
 * @author Brendon Butler
 */
public abstract class SubCommand extends Notifiable implements CommandExecutor {

    /**
     * The itemResource parameter is used to identify the items based on the player's inventory
     */
    protected static final Parameter.Value<ResourceKey> itemResource = Parameter.resourceKey().key("item")
            .addParser((Parameter.Key<? super ResourceKey> parameterKey,
                        ArgumentReader.Mutable reader,
                        CommandContext.Builder context) -> {
                String input = reader.parseString();
                ResourceKey resourceKey = ResourceKey.resolve(input);

                if (resourceKey != null && Sponge.game().registry(ItemTypes.registry().type()).findValue(resourceKey).isPresent()) {
                    return Optional.of(resourceKey);
                } else {
                    throw reader.createException(Component.text("Invalid item: " + input));
                }
            })
            .completer((context, input) -> {
                Player player = context.cause().first(Player.class).orElse(null);
                if (player != null && !context.hasPermission("shops.add.any")) {
                    return player.inventory().slots().stream()
                            .filter(slot -> !slot.peek().isEmpty())
                            .map(slot -> slot.peek().type())
                            .filter(itemType -> itemType.key(itemType.registryType()).asString().startsWith(input))
                            .map(itemType -> CommandCompletion.of(itemType.key(itemType.registryType()).asString()))
                            .toList();
                } else {
                    return Sponge.game().registry(ItemTypes.registry().type()).stream()
                            .filter(i -> i.key(i.registryType()).asString().startsWith(input))
                            .map(i -> CommandCompletion.of(i.key(i.registryType()).asString()))
                            .toList();
                }
            })
            .build();

    /**
     * The itemStoreResource parameter is used to identify the items based on the store's inventory
     */
    protected static final Parameter.Value<ResourceKey> itemStoreResource = Parameter.resourceKey().key("item")
            .addParser((Parameter.Key<? super ResourceKey> parameterKey,
                        ArgumentReader.Mutable reader,
                        CommandContext.Builder context) -> {
                String input = reader.parseString();
                ResourceKey resourceKey = ResourceKey.resolve(input);

                if (resourceKey != null && Sponge.game().registry(ItemTypes.registry().type()).findValue(resourceKey).isPresent()) {
                    return Optional.of(resourceKey);
                } else {
                    throw reader.createException(Component.text("Invalid item: " + input));
                }
            })
            .completer((context, input) -> {
                Player player = context.cause().first(Player.class).orElse(null);
                Optional<Store> store = player != null ? InventoryManagementSystem.locateCurrentStore(player) : Optional.empty();

                return store.map(value -> value.getItems().keySet().stream()
                        .filter(i -> i.key(i.registryType()).asString().startsWith(input))
                        .map(i -> CommandCompletion.of(i.key(i.registryType()).asString()))
                        .toList()).orElse(null);
            })
            .build();

    /**
     * The priceParameter is used to identify the price of the item to be added to the store
     */
    protected static final Parameter.Value.Builder<BigDecimal> priceParameter = Parameter.bigDecimal();

    /**
     * The quantityWithAllParameter is used to identify a quantity for items in a player's inventory
     */
    protected static final Parameter.Value.Builder<Integer> quantityWithAllParameter = Parameter.integerNumber()
            .addParser((Parameter.Key<? super Integer> parameterKey,
                        ArgumentReader.Mutable reader,
                        CommandContext.Builder context) -> {
                ItemType itemType = ItemTypes.registry().value(context.requireOne(itemResource));
                String input = reader.parseString();
                int quantity = 0;

                if (input.isEmpty()) return Optional.of(quantity);

                if (input.equalsIgnoreCase("all")) {
                    quantity = ((ServerPlayer) context.subject()).inventory().query(
                            ITEM_TYPE.get().of(itemType)
                    ).totalQuantity();
                } else {
                    try {
                        quantity = Integer.parseInt(input);
                    } catch (NumberFormatException ignored) {}
                }

                return Optional.of(quantity);
            })
            .completer((context, input) -> {
                if (input.isEmpty()) {
                    return List.of(CommandCompletion.of("quantity"), CommandCompletion.of("all"));
                } else {
                    return Stream.of(CommandCompletion.of(input), CommandCompletion.of("all"))
                            .filter(i -> {
                                try {
                                    return ("all".startsWith(input) || NumberFormat.getInstance().parse(input) instanceof Integer);
                                } catch (ParseException ignore) {}
                                return true;
                            })
                            .collect(Collectors.toList());
                }
            });

    /**
     * The quantityStoreWithAllParameter is used to identify a quantity for items in a store that a user is in
     */
    protected static final Parameter.Value.Builder<Integer> quantityStoreWithAllParameter = Parameter.integerNumber()
            .addParser((Parameter.Key<? super Integer> parameterKey,
                        ArgumentReader.Mutable reader,
                        CommandContext.Builder context) -> {
                ItemType itemType = ItemTypes.registry().value(context.requireOne(itemStoreResource));
                String input = reader.parseString();
                int quantity = 0;

                if (input.isEmpty()) return Optional.of(quantity);
                Optional<Store> store = InventoryManagementSystem.locateCurrentStore((ServerPlayer) context.subject());

                if (input.equalsIgnoreCase("all") && store.isPresent()) {
                    quantity = InventoryManagementSystem.countQuantity(store.get(), itemType);
                } else {
                    try {
                        quantity = Integer.parseInt(input);
                    } catch (NumberFormatException ignored) {}
                }

                return Optional.of(quantity);
            })
            .completer((context, input) -> {
                if (input.isEmpty()) {
                    return List.of(CommandCompletion.of("quantity"), CommandCompletion.of("all"));
                } else {
                    return Stream.of(CommandCompletion.of(input), CommandCompletion.of("all"))
                            .filter(i -> {
                                try {
                                    return ("all".startsWith(input) ||
                                            NumberFormat.getInstance().parse(input) instanceof Integer);
                                } catch (ParseException ignore) {
                                }
                                return true;
                            })
                            .collect(Collectors.toList());
                }
            });

    /**
     * The quantityParameter is used to identify a quantity for items in a store based on the player's inventory
     */
    protected static final Parameter.Value.Builder<Integer> quantityParameter = Parameter.integerNumber()
            .addParser((Parameter.Key<? super Integer> parameterKey,
                        ArgumentReader.Mutable reader,
                        CommandContext.Builder context) -> {
                String input = reader.parseString();
                int quantity = 0;

                if (input.isEmpty()) return Optional.of(quantity);

                try {
                    quantity = Integer.parseInt(input);
                } catch (NumberFormatException ignored) {}

                return Optional.of(quantity);
            });

    /**
     * The coordinateParameter is used to identify the coordinates for the store location
     */
    protected static final Parameter.Value.Builder<Double> coordinateParameter = Parameter.doubleNumber()
            .addParser((Parameter.Key<? super Double> parameterKey,
                        ArgumentReader.Mutable reader,
                        CommandContext.Builder context) -> {
                String input = reader.parseString();
                double coordinate = 0;

                if (input.isEmpty()) return Optional.of(coordinate);

                try {
                    coordinate = Double.parseDouble(input);
                } catch (NumberFormatException ignored) {}

                return Optional.of(coordinate);
            });

    protected static final Parameter.Value<Store> inputStore = Parameter
            .builder(Store.class)
            .addParser((Parameter.Key<? super Store> parameterKey,
                        ArgumentReader.Mutable reader,
                        CommandContext.Builder context) -> {
                Optional<Store> store = Optional.empty();

                String storeName = reader.parseString();

                try {
                    store = identifyStore((storeName.contains("-") ? storeName.replaceFirst("-", "~") : storeName));
                } catch (Core.MultipleStoresMatchedException exception) {
                    Notifier.process(context.cause(), STORE_MULTI_MATCH, null);
                }

                return store;
            })
            .completer((context, input) -> Store.STORES.stream()
                    .filter(s -> {
                        Optional<ServerPlayer> player = (context.subject() instanceof ServerPlayer) ? Optional.of((ServerPlayer) context.subject()) : Optional.empty();

                        List<Store> identifiedStores = (!input.isEmpty() ? identifyStores(input) : Store.STORES).stream().filter(i ->
                                (context.hasPermission("shop.delete.all"))
                                || (player.isPresent()) && i.getOwner().equals(player.get().uniqueId())
                                || (player.isEmpty())
                        ).toList();

                        return identifiedStores.contains(s);
                    })
                    .map(s -> CommandCompletion.of(String.format("%s-%s", s.getName(), s.getUUID())))
                    .toList())
            .key("store")
            .build();

    protected static final Flag force = Flag.builder().alias("f").build();
    protected static final Flag forceHard = Flag.builder().alias("FORCE").build();

    /**
     * Holds any stores that are identified based on the identifyStore method
     */
    protected List<Store> stores = new ArrayList<>();

    /**
     * The identifyStore method is a common method for identifying a store based on a string name, UUID or a combination
     * using the format name~UUID
     *
     * @param nameOrUUID input name or UUID
     * @return the optional store if found or optional empty if not found or duplicates are found
     */
    protected static Optional<Store> identifyStore(String nameOrUUID) throws Core.MultipleStoresMatchedException {
        return Store.identifyStore(nameOrUUID);
    }

    /**
     * The identifyStores method is used to identify multiple stores based on a string name, UUID or a combination using
     * the format name~UUID
     *
     * @param nameOrUUID input name or UUID
     * @return the list of stores identified by the provided input string
     */
    protected static List<Store> identifyStores(String nameOrUUID) {
        return Store.STORES.stream()
                .filter(s -> s.getName().contains(nameOrUUID)
                             || s.getUUID().toString().contains(nameOrUUID)
                             || String.format("%s~%s", s.getName(), s.getUUID()).contains(nameOrUUID))
                .toList();
    }
}