package net.sparkzz.shops.command.sub;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.Cuboid;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.NO_STORE_FOUND;

/**
 * Update subcommand used for updating item attributes in a shop
 *
 * @author Brendon Butler
 */
public class UpdateCommand extends SubCommand {

    private static final Parameter.Value<String> materialParameter = Parameter
            .string()
            .completer(
                    (context, input) -> Sponge.game().registry(RegistryTypes.ITEM_TYPE).stream()
                            .map(itemType -> CommandCompletion.of(itemType.key(itemType.registryType()).asString()))
                            .toList()
            )
            .key("material")
            .build();
    private static final Parameter.Value<ServerLocation> locationParameter = Parameter.location().requiredPermission("shops.update.location").key("starting-point").build();
    private static final Parameter.Value<Vector3d> endingPointParameter = Parameter.vector3d().requiredPermission("shops.update.location").key("ending-point").build();
    private static final Parameter.Value<String> storeNameParameter = Parameter.remainingJoinedStrings().key("store-name").build();
    private static final Parameter.Value<Boolean> infiniteFundsParameter = Parameter.bool().requiredPermission("shops.update.inf-funds").key("infinite-funds").build();
    private static final Parameter.Value<Boolean> infiniteStockParameter = Parameter.bool().requiredPermission("shops.update.inf-stock").key("infinite-stock").build();

    @Override
    public CommandResult execute(@NotNull CommandContext context) throws NumberFormatException {
        resetAttributes();
        ServerPlayer player = (ServerPlayer) setAttribute("sender", context.subject());
        Store store = setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));

        if (store == null) {
            return CommandResult.error(Component.text(Notifier.compose(NO_STORE_FOUND, getAttributes())));
        }

        if (context.one(storeNameParameter).isPresent()) {
            setAttribute("arg1", "store-name");
            String storeName = setAttribute("arg2", context.requireOne(storeNameParameter));
            store.setName(storeName);
            Notifier.process(player, Notifier.CipherKey.STORE_UPDATE_SUCCESS, getAttributes());
        } else if (context.one(materialParameter).isPresent()) {
            setAttribute("arg1", "material");
            Optional<ItemType> optionalItemType = Sponge.game().registry(RegistryTypes.ITEM_TYPE).findValue(ResourceKey.of(context.requireOne(materialParameter).split(":")[0], context.requireOne(materialParameter).split(":")[1]));
            ItemType material = setAttribute("arg2", optionalItemType.orElse(null));
            Parameter.Value<BigDecimal> buyPriceParameter = Parameter.bigDecimal().key("customer-buy-price").build();
            Parameter.Value<BigDecimal> sellPriceParameter = Parameter.bigDecimal().key("customer-sell-price").build();
            Parameter.Value<Integer> quantityParameter = Parameter.integerNumber().key("max-quantity").build();
            Parameter.Value<Boolean> infiniteQuantityParameter = Parameter.bool().key("infinite-quantity").build();

            if (context.one(buyPriceParameter).isPresent()) {
                System.out.println("buyPriceParameter: " + buyPriceParameter);
                setAttribute("arg3", "customer-buy-price");
                BigDecimal buyPrice = setAttribute("arg2", context.requireOne(buyPriceParameter));
                store.setAttribute(material, "customer-buy-price", buyPrice);
                Notifier.process(player, Notifier.CipherKey.STORE_UPDATE_SUCCESS, getAttributes());
            } else if (context.one(sellPriceParameter).isPresent()) {
                System.out.println("sellPriceParameter: " + sellPriceParameter);
                setAttribute("arg3", "customer-sell-price");
                BigDecimal sellPrice = setAttribute("arg4", context.requireOne(sellPriceParameter));
                store.setAttribute(material, "customer-sell-price", sellPrice);
                Notifier.process(player, Notifier.CipherKey.STORE_UPDATE_SUCCESS, getAttributes());
            } else if (context.one(quantityParameter).isPresent()) {
                System.out.println("quantityParameter: " + quantityParameter);
                setAttribute("arg3", "max-quantity");
                int maxQuantity = setAttribute("arg4", context.requireOne(quantityParameter));

                if (maxQuantity < -1) {
                    return CommandResult.error(Component.text(Notifier.compose(Notifier.CipherKey.INVALID_QUANTITY, Map.of("quantity", maxQuantity))));
                }

                store.setAttribute(material, "max-quantity", maxQuantity);
                Notifier.process(player, Notifier.CipherKey.STORE_UPDATE_SUCCESS, getAttributes());
            } else if (context.one(infiniteQuantityParameter).isPresent()) {
                System.out.println("infiniteQuantityParameter: " + infiniteQuantityParameter);
                setAttribute("arg3", "infinite-quantity");
                boolean infiniteQuantity = setAttribute("arg4", context.requireOne(infiniteQuantityParameter));
                store.setAttribute(material, "infinite-quantity", Boolean.compare(infiniteQuantity, false));
                Notifier.process(player, Notifier.CipherKey.STORE_UPDATE_SUCCESS, getAttributes());
            } else {
                return CommandResult.error(Component.text("No valid material attributes provided."));
            }
        } else if (context.one(infiniteFundsParameter).isPresent()) {
            setAttribute("arg1", "infinite-funds");
            boolean infiniteFunds = setAttribute("arg2", context.requireOne(infiniteFundsParameter));
            store.setInfiniteFunds(infiniteFunds);
            Notifier.process(player, Notifier.CipherKey.STORE_UPDATE_SUCCESS, getAttributes());
        } else if (context.one(infiniteStockParameter).isPresent()) {
            setAttribute("arg1", "infinite-stock");
            boolean infiniteStock = setAttribute("arg2", context.requireOne(infiniteStockParameter));
            store.setInfiniteStock(infiniteStock);
            Notifier.process(player, Notifier.CipherKey.STORE_UPDATE_SUCCESS, getAttributes());
        } else if (context.one(locationParameter).isPresent()) {
            setAttribute("arg1", "location");
            ServerLocation startingPoint = setAttribute("start-point", context.requireOne(locationParameter));
            Vector3d endingPoint = setAttribute("end-point", context.requireOne(endingPointParameter));
            store.setCuboidLocation(new Cuboid(startingPoint, endingPoint));
            Notifier.process(player, Notifier.CipherKey.STORE_UPDATE_SUCCESS_LOCATION, getAttributes());
        }

        return CommandResult.success();
    }

    /**
     * Build the Command structure to be registered
     */
    public static Command.Parameterized build() {
        final Command.Parameterized storeName = Command.builder()
                .executor(new UpdateCommand())
                .addParameter(storeNameParameter)
                .build();

        final Command.Parameterized location = Command.builder()
                .executor(new UpdateCommand())
                .addParameters(locationParameter, endingPointParameter)
                .build();

        final Command.Parameterized customerBuyPrice = Command.builder()
                .executor(new UpdateCommand())
                .addParameter(Parameter.bigDecimal().key("customer-buy-price").build())
                .build();

        final Command.Parameterized customerSellPrice = Command.builder()
                .executor(new UpdateCommand())
                .addParameter(Parameter.bigDecimal().key("customer-sell-price").build())
                .build();

        final Command.Parameterized maxQuantity = Command.builder()
                .executor(new UpdateCommand())
                .addParameter(Parameter.integerNumber().key("max-quantity").build())
                .build();

        final Command.Parameterized infiniteQuantity = Command.builder()
                .executor(new UpdateCommand())
                .addParameter(Parameter.bool().key("infinite-quantity").build())
                .build();

        final Command.Parameterized materialAttributes = Command.builder()
                .executor(new UpdateCommand())
                .addChild(customerBuyPrice, "customer-buy-price")
                .addChild(customerSellPrice, "customer-sell-price")
                .addChild(maxQuantity, "max-quantity")
                .addChild(infiniteQuantity, "infinite-quantity")
                .build();

        final Command.Parameterized material = Command.builder()
                .executor(new UpdateCommand())
                .addParameter(materialParameter)
                .addChild(materialAttributes, Sponge.game().registry(RegistryTypes.ITEM_TYPE).stream()
                        .map(itemType -> itemType.key(itemType.registryType()).asString())
                        .toList())
                .build();

        final Command.Parameterized infiniteFunds = Command.builder()
                .executor(new UpdateCommand())
                .addParameter(infiniteFundsParameter)
                .build();

        final Command.Parameterized infiniteStock = Command.builder()
                .executor(new UpdateCommand())
                .addParameter(infiniteStockParameter)
                .build();

        return Command.builder()
                .executor(new UpdateCommand())
                .permission("shops.cmd.update")
                .shortDescription(Component.text("Allows a player to update attributes in their store"))
                .extendedDescription(Component.text("Update attributes in your store"))
                .executionRequirements(context -> context.cause().root() instanceof ServerPlayer)
                .addChild(storeName, "store-name")
                //.addChild(material, "material")
                .addChild(infiniteFunds, "infinite-funds")
                .addChild(infiniteStock, "infinite-stock")
                .addChild(location, "location")
                .build();
    }
}