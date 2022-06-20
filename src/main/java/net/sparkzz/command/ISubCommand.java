package net.sparkzz.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Interface for sub command layout
 *
 * @author Brendon Butler
 */
public interface ISubCommand {

    boolean process(CommandSender sender, Command command, String label, String[] args);
}