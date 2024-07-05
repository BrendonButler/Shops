package net.sparkzz.shops.command.sub;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;

import java.util.Optional;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;

/**
 * Remove subcommand used for removing items from a shop
 *
 * @author Brendon Butler
 */
public class RemoveCommand extends SubCommand {

    public CommandResult execute(@NotNull CommandContext context) throws NumberFormatException {
        resetAttributes();
        ResourceKey itemResourceKey = context.requireOne(itemStoreResource);
        Optional<ItemType> optionalItemType = Sponge.game().registry(ItemTypes.registry().type()).findValue(itemResourceKey);
        setAttribute("material", itemResourceKey);
        ServerPlayer player = (ServerPlayer) setAttribute("sender", context.subject());
        Store store = setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));
        int quantity = setAttribute("quantity", context.one(quantityStoreWithAllParameter.key("quantity").build()).orElse(0));

        if (store == null)
            return CommandResult.error(Component.text(Notifier.compose(NO_STORE_FOUND, getAttributes())));

        if (optionalItemType.isEmpty())
            return CommandResult.error(Component.text(Notifier.compose(INVALID_MATERIAL, getAttributes())));

        ItemType material = optionalItemType.get();

        if (!store.containsItemType(material))
            return CommandResult.error(Component.text(Notifier.compose(MATERIAL_MISSING_STORE, getAttributes())));

        if (quantity < 0)
            return CommandResult.error(Component.text(Notifier.compose(INVALID_QUANTITY, getAttributes())));

        InventoryTransactionResult result;

        if (quantity == 0) {
            if (context.hasFlag(force)) {
                store.removeItem(material);
                Notifier.process(player, REMOVE_SUCCESS, getAttributes());
                return CommandResult.success();
            } else {
                quantity = store.getItems().get(material).get("quantity").intValue();
            }

            if (!InventoryManagementSystem.canInsert(player, material, quantity))
                return CommandResult.error(Component.text(Notifier.compose(REMOVE_INSUFFICIENT_INV_PLAYER, getAttributes())));

            store.removeItem(material);
            result = player.inventory().offer(ItemStack.of(material, quantity));

            if (!result.type().equals(InventoryTransactionResult.Type.SUCCESS)) {
                store.addItem(material, quantity - result.rejectedItems().stream().mapToInt(ItemStackSnapshot::quantity).sum());
                return CommandResult.error(Component.text(Notifier.compose(REMOVE_INSUFFICIENT_INV_PLAYER, getAttributes())));
            }

            Notifier.process(player, REMOVE_SUCCESS, getAttributes());
            return CommandResult.success();
        }

        boolean canInsertPlayer = InventoryManagementSystem.canInsert(player, material, quantity);

        if (!context.hasFlag(force) && !canInsertPlayer)
            return CommandResult.error(Component.text(Notifier.compose(REMOVE_INSUFFICIENT_INV_PLAYER, getAttributes())));
        else if (canInsertPlayer)
            result = player.inventory().offer(ItemStack.of(material, quantity));
        else result = InventoryTransactionResult.builder().type(InventoryTransactionResult.Type.SUCCESS).build();

        if (store.getItems().get(material).get("quantity").longValue() < quantity)
            return CommandResult.error(Component.text(Notifier.compose(INSUFFICIENT_INV_STORE, getAttributes())));

        if (!result.type().equals(InventoryTransactionResult.Type.SUCCESS)) {
            store.addItem(material, quantity - result.rejectedItems().stream().mapToInt(ItemStackSnapshot::quantity).sum());
            return CommandResult.error(Component.text(Notifier.compose(REMOVE_INSUFFICIENT_INV_PLAYER, getAttributes())));
        }

        store.removeItem(material, quantity);
        Notifier.process(player, REMOVE_SUCCESS_QUANTITY, getAttributes());
        return CommandResult.success();
    }

    /**
     * Build the Command structure to be registered
     */
    public static Command.Parameterized build() {
        return Command.builder()
                .executor(new RemoveCommand())
                .permission("shops.cmd.remove")
                .shortDescription(Component.text("Allows a player to remove items from their store"))
                .extendedDescription(Component.text("Remove items from your store"))
                .executionRequirements(context -> context.cause().root() instanceof ServerPlayer)
                .addParameter(itemStoreResource)
                .addParameter(quantityStoreWithAllParameter.key("quantity").optional().build())
                .addFlags(force)
                .build();
    }
}