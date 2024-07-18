package net.sparkzz.shops.command;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.command.sub.AddCommand;
import net.sparkzz.shops.command.sub.BrowseCommand;
import net.sparkzz.shops.command.sub.CreateCommand;
import net.sparkzz.shops.command.sub.DeleteCommand;
import net.sparkzz.shops.command.sub.RemoveCommand;
import net.sparkzz.shops.command.sub.UpdateCommand;
import net.sparkzz.shops.util.Notifier;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Shop Command for browsing/buying/selling/updating items in the current store
 *
 * @author Brendon Butler
 */
public class ShopCommand implements CommandExecutor {

    private static final Map<Iterable<String>, Command.Parameterized> subCommands = new HashMap<>() {{
        put(Collections.singletonList("add"), AddCommand.build());
        put(Collections.singletonList("browse"), BrowseCommand.build());
//        put(Collections.singletonList("buy"), BuyCommand.build());
        put(Collections.singletonList("create"), CreateCommand.build());
        put(Collections.singletonList("delete"), DeleteCommand.build());
//        put(Collections.singletonList("deposit"), DepositCommand.build());
//        put(Collections.singletonList("sell"), SellCommand.build());
//        put(Collections.singletonList("transfer"), TransferCommand.build());
        put(Collections.singletonList("remove"), RemoveCommand.build());
        put(Collections.singletonList("update"), UpdateCommand.build());
//        put(Collections.singletonList("withdraw"), WithdrawCommand.build());
    }};

    /**
     * The base command for all shop user subcommands
     *
     * @param context the CommandContext
     * @return the command result
     */
    public CommandResult execute(CommandContext context) {
        return CommandResult.error(Component.text(Notifier.compose(Notifier.CipherKey.INVALID_ARG_CNT, null)));
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