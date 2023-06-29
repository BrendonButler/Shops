package net.sparkzz.command;

import net.sparkzz.shops.Shops;
import net.sparkzz.util.Notifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shops Command for plugin info/credits
 *
 * @author Brendon Butler
 */
public class InfoCommand extends CommandManager {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("shops.cmd.shops")) {
            Notifier.process(sender, Notifier.CipherKey.NO_PERMS_CMD);
            return true;
        }

        sender.sendMessage(String.format("§l§3Shops v%s", Shops.getDesc().getVersion()));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        return null;
    }
}