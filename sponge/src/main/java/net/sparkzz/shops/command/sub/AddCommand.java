package net.sparkzz.shops.command.sub;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;
import static org.spongepowered.api.item.inventory.query.QueryTypes.ITEM_TYPE;

/**
 * Add subcommand used for adding items to a shop
 *
 * @author Brendon Butler
 */
public class AddCommand extends SubCommand {

    private static final Parameter.Value<ResourceKey> itemResource = Parameter.resourceKey().key("item")
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



    private static final Parameter.Value.Builder<BigDecimal> priceParameter = Parameter.bigDecimal();

    private static final Parameter.Value.Builder<Integer> quantityParameter = Parameter.integerNumber()
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

    public CommandResult execute(CommandContext context) throws NumberFormatException {
        resetAttributes();
        ResourceKey itemResourceKey = context.requireOne(itemResource);
        Optional<ItemType> optionalItemType = Sponge.game().registry(ItemTypes.registry().type()).findValue(itemResourceKey);
        setAttribute("material", itemResourceKey);
        ServerPlayer player = (ServerPlayer) setAttribute("sender", context.subject());
        Store store = setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));
        int quantity = setAttribute("quantity", context.one(quantityParameter.key("quantity").build()).orElse(0));

        if (store == null)
            return CommandResult.error(Component.text(Notifier.compose(NO_STORE_FOUND, getAttributes())));

        if (optionalItemType.isEmpty())
            return CommandResult.error(Component.text(Notifier.compose(INVALID_MATERIAL, getAttributes())));

        ItemType material = optionalItemType.get();

        if (quantity < 0 && !player.hasPermission("shops.update.inf-stock"))
            return CommandResult.error(Component.text(Notifier.compose(NO_PERMS_INF_STOCK, getAttributes())));

        if (quantity == 0 && store.containsItemType(material))
            return CommandResult.error(Component.text(Notifier.compose(MATERIAL_EXISTS_STORE, getAttributes())));

        if (!InventoryManagementSystem.canRemove(player, ItemStack.of(material), quantity) && !player.hasPermission("shops.add.inf"))
            return CommandResult.error(Component.text(Notifier.compose(INSUFFICIENT_STOCK_PLAYER, getAttributes())));

        Optional<Map<String, Number>> storeItemStack = store.containsItemType(material) ? Optional.of(store.getItems().get(material)) : Optional.empty();

        if (storeItemStack.isPresent()
            && !store.hasInfiniteStock()
            && (long) storeItemStack.get().getOrDefault("max_quantity", -1) != -1
            && (long) storeItemStack.get().getOrDefault("max_quantity", 0) < (long) storeItemStack.get().getOrDefault("quantity", 0) + quantity)
            return CommandResult.error(Component.text("The store doesn't currently have enough capacity to add these items!")); // TODO: add new key to Notifier

        Optional<BigDecimal> buyPriceOptional = context.one(priceParameter.key("customer-buy-price").build());
        Optional<BigDecimal> sellPriceOptional = context.one(priceParameter.key("customer-sell-price").build());
        Optional<Integer> maxQuantityOptional = context.one(quantityParameter.key("max-quantity").build());

        BigDecimal buyPrice = setAttribute("buy-price", buyPriceOptional.orElse(BigDecimal.valueOf(-1)));
        BigDecimal sellPrice = setAttribute("sell-price", sellPriceOptional.orElse(BigDecimal.valueOf(-1)));
        int maxQuantity = setAttribute("max-quantity", maxQuantityOptional.orElse(0));

        player.inventory().query(ITEM_TYPE.get().of(material)).poll(quantity);

        if (store.containsItemType(material)) {
            store.addItem(material, quantity);
            Notifier.process(player, (quantity > 0 ? ADD_SUCCESS_QUANTITY : ADD_SUCCESS), getAttributes());
            return CommandResult.success();
        }

        store.addItem(material, quantity, maxQuantity, buyPrice, sellPrice);
        Notifier.process(player, (quantity > 0 ? ADDED_MATERIAL_TO_STORE_QUANTITY : ADDED_MATERIAL_TO_STORE), getAttributes());
        return CommandResult.success();
    }

    /**
     * Build the Command structure to be registered
     */
    public static Command.Parameterized build() {
        return Command.builder()
                .executor(new AddCommand())
                .permission("shops.cmd.add")
                .shortDescription(Component.text("Allows a player to add items to their store"))
                .extendedDescription(Component.text("Add items to your store"))
                .executionRequirements(context -> context.cause().root() instanceof ServerPlayer)
                .addParameter(itemResource)
                .addParameter(quantityParameter.key("quantity").optional().build())
                .addParameter(priceParameter.key("customer-buy-price").optional().build())
                .addParameter(priceParameter.key("customer-sell-price").optional().build())
                .addParameter(quantityParameter.key("max-quantity").optional().build())
                .build();
    }
}