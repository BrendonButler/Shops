package net.sparkzz.shops.command.sub;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.Store;
import net.sparkzz.shops.command.SubCommand;
import net.sparkzz.shops.util.InventoryManagementSystem;
import net.sparkzz.shops.util.Notifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;

/**
 * Browse subcommand used for browsing items in a shop
 *
 * @author Brendon Butler
 */
public class BrowseCommand extends SubCommand {

    // TODO: improve page browsing layout
    public CommandResult execute(@NotNull CommandContext context) throws NumberFormatException {
        resetAttributes();
        ServerPlayer player = (ServerPlayer) setAttribute("sender", context.subject());
        Store store = setAttribute("store", InventoryManagementSystem.locateCurrentStore(player).orElse(null));
        int pageNumber = setAttribute("page-number", context.one(quantityParameter.key("page-number").optional().build()).orElse(1));

        if (store == null)
            return CommandResult.error(Component.text(Notifier.compose(NO_STORE_FOUND, getAttributes())));

        if (store.getItems().isEmpty())
            return CommandResult.error(Component.text(Notifier.compose(STORE_NO_ITEMS, getAttributes())));

        String page = Notifier.Paginator.buildBrowsePage(store, pageNumber);

        if (page == null)
            return CommandResult.error(Component.text(Notifier.compose(INVALID_PAGE_NUM, getAttributes())));

        player.sendMessage(Component.text(page));
        return CommandResult.success();
    }

    /**
     * Build the Command structure to be registered
     */
    public static Command.Parameterized build() {
        return Command.builder()
                .executor(new BrowseCommand())
                .permission("shops.cmd.browse")
                .shortDescription(Component.text("Allows a player to browse items in a store"))
                .extendedDescription(Component.text("Browse items in a store"))
                .executionRequirements(context -> context.cause().root() instanceof ServerPlayer)
                .addParameter(quantityParameter.key("page-number").optional().build())
                .build();
    }
}