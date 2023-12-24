package net.sparkzz.shops.command;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.util.Notifier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

/**
 * Shops Command for plugin info/credits
 *
 * @author Brendon Butler
 */
public class InfoCommand extends CommandManager {

    /**
     * The base command for the plugin to provide plugin details
     *
     * @param context the CommandContext
     * @return whether the command was successful
     */
    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        CommandCause cause = context.cause();

        if (!cause.hasPermission("shops.cmd.shops")) {
            Notifier.process(cause, Notifier.CipherKey.NO_PERMS_CMD, null);
            return CommandResult.success();
        }

        cause.sendMessage(Component.text(String.format("§l§3Shops v%s", Sponge.pluginManager().plugin("shops").get().metadata().version())));
        return CommandResult.success();
    }
}