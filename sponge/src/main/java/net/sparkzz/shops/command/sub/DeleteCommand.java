package net.sparkzz.shops.command.sub;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.Core;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.EconomyService;

import java.math.BigDecimal;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;

/**
 * Delete subcommand used for deleting a shop
 *
 * @author Brendon Butler
 */
public class DeleteCommand extends SubCommand {

    public CommandResult execute(@NotNull CommandContext context) {
        ServerPlayer player = (ServerPlayer) context.subject();

        try {
            resetAttributes();

            Store store = context.requireOne(inputStore);
            setAttribute("sender", context.subject());
            setAttribute("store", store);

            if (store == null) {
                Notifier.process(player, STORE_NO_STORE_FOUND, getAttributes());
                return CommandResult.success();
            }

            boolean ignoreInv = false, ignoreFunds = false;

            // TODO: determine a way to check if a player can remove all items from the shop, if they can, remove them all
            // TODO: add force flags (-f will ignore all inventory, then process) (-F will ignore all inventory and finances, then process)
            if (context.hasFlag(force)) {
                ignoreInv = true;
            } else if (context.hasFlag(forceHard)) {
                ignoreInv = true;
                ignoreFunds = true;
            }

            boolean canInsertAll = false;

            setAttribute("store", store.getName());

            if (!ignoreInv)
                canInsertAll = store.getItems().isEmpty() || InventoryManagementSystem.canInsertAll(player, store.getItems().entrySet().stream()
                        .map(entry -> ItemStack.of(entry.getKey(), (int) entry.getValue().getOrDefault("quantity", 0)))
                        .toList());

            if (!ignoreInv && !canInsertAll) {
                Notifier.process(player, STORE_DELETE_INSUFFICIENT_INV_PLAYER, getAttributes());
                return CommandResult.success();
            }

            if (!ignoreFunds) {
                EconomyService econ = Sponge.server().serviceProvider().economyService().orElseThrow();
                econ.findOrCreateAccount(player.uniqueId()).orElseThrow().deposit(econ.defaultCurrency(), store.getBalance());
                store.setBalance(BigDecimal.ZERO);
            }

            boolean success = Store.STORES.remove(store);

            if (success)
                Notifier.process(player, STORE_DELETE_SUCCESS, getAttributes());
            else Notifier.process(player, STORE_DELETE_FAIL, getAttributes());
        } catch (Core.MultipleStoresMatchedException exception) {
            Notifier.process(player, STORE_MULTI_MATCH, getAttributes());
        }
        return CommandResult.success();
    }

    /**
     * Build the Command structure to be registered
     */
    public static Command.Parameterized build() {
        return Command.builder()
                .executor(new DeleteCommand())
                .permission("shops.cmd.delete")
                .shortDescription(Component.text("Allows a player to delete stores"))
                .extendedDescription(Component.text("Delete a store for players"))
                .executionRequirements(context -> context.cause().root() instanceof ServerPlayer)
                .addParameters(inputStore)
                .addFlags(force, forceHard)
                .build();
    }
}