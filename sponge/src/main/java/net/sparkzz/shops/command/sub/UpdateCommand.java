package net.sparkzz.shops.command.sub;

import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.Cuboid;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.NO_STORE_FOUND;

/**
 * Update subcommand used for updating item attributes in a shop
 *
 * @author Brendon Butler
 */
public class UpdateCommand extends SubCommand {

    private static final Parameter.Value<ItemType> materialParameter = Parameter.registryElement(TypeToken.get(ItemType.class), RegistryTypes.ITEM_TYPE).key("material").build();
    private static final Parameter.Value<ServerLocation> locationParameter = Parameter.location().requiredPermission("shops.update.location").key("starting-point").build();
    private static final Parameter.Value<Vector3d> endingPointParameter = Parameter.vector3d().requiredPermission("shops.update.location").key("ending-point").build();
    private static final Parameter.Value<String> storeNameParameter = Parameter.remainingJoinedStrings().key("store-name").build();
    private static final Parameter.Value<Boolean> infiniteFundsParameter = Parameter.bool().requiredPermission("shops.update.inf-funds").key("infinite-funds").build();
    private static final Parameter.Value<Boolean> infiniteStockParameter = Parameter.bool().requiredPermission("shops.update.inf-stock").key("infinite-stock").build();

    public CommandResult execute(@NotNull CommandContext context) throws NumberFormatException {
        resetAttributes();
        ServerPlayer player = (ServerPlayer) setAttribute("sender", context.subject());
        Store store = setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));

        if (store == null)
            return CommandResult.error(Component.text(Notifier.compose(NO_STORE_FOUND, getAttributes())));

        if (context.one(storeNameParameter).isPresent()) {
            setAttribute("arg1", "store-name");
            String storeName = setAttribute("arg2", context.requireOne(storeNameParameter));
            store.setName(storeName);
            Notifier.process(player, Notifier.CipherKey.STORE_UPDATE_SUCCESS, getAttributes());
        } else if (context.one(materialParameter).isPresent()) {
            setAttribute("arg1", "material");
            ItemType material = setAttribute("arg2", context.requireOne(materialParameter));
            // update the store's material attributes
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

        final Command.Parameterized material = Command.builder()
                .executor(new UpdateCommand())
                .addParameters(
                        materialParameter,
                        storeItemAttributeParameter.key("material-attribute").build()
                ).build();

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
                .addChild(material, "material")
                .addChild(infiniteFunds, "infinite-funds")
                .addChild(infiniteStock, "infinite-stock")
                .addChild(location, "location")
                .build();
    }
}