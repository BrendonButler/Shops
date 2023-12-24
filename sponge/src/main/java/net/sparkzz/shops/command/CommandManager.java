package net.sparkzz.shops.command;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.sparkzz.shops.util.Notifiable;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

/**
 * Class for Command layout
 *
 * @author Brendon Butler
 */
public abstract class CommandManager extends Notifiable implements CommandExecutor {

    @Inject
    PluginContainer container;

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event){
        event.register(this.container, Command
                .builder()
                .executor(new InfoCommand())
                .permission("shops.cmd.shops")
                .shortDescription(Component.text("Hello World Command"))
                .executionRequirements(context -> context.cause().root() instanceof ServerPlayer)
                .build(), "helloworld", "hello", "test");
    }
}