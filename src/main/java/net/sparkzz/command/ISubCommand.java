package net.sparkzz.command;

import net.sparkzz.util.Notifiable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Interface for sub command layout
 *
 * @author Brendon Butler
 */
public abstract class ISubCommand extends Notifiable {

    public abstract boolean process(CommandSender sender, Command command, String label, String[] args);
}