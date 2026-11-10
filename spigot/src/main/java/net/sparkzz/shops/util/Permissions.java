package net.sparkzz.shops.util;

import net.sparkzz.shops.AbstractStore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class Permissions extends AbstractPermissions<CommandSender> {

    private static final Permissions INSTANCE = new Permissions();

    private Permissions() {}

    public static boolean isPermitted(CommandSender sender, ShopAction action) {
        return INSTANCE.evaluate(sender, action).isEmpty();
    }

    public static boolean isPermitted(CommandSender sender, ShopAction action, AbstractStore store) {
        return INSTANCE.evaluate(sender, action, store).isEmpty();
    }

    public static Optional<AbstractNotifier.CipherKey> denialFor(CommandSender sender, ShopAction action, AbstractStore store) {
        return INSTANCE.evaluate(sender, action, store);
    }

    @Override
    protected boolean hasNode(CommandSender sender, String node) {
        return sender.hasPermission(node);
    }

    @Override
    protected Optional<UUID> getId(CommandSender sender) {
        return (sender instanceof Player player) ? Optional.of(player.getUniqueId()) : Optional.empty();
    }
}
