package net.sparkzz.shops;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.command.CommandSender;

public class TestHelper {

    public static void performCommand(CommandSender sender, String message) {
        MockBukkit.getOrCreateMock().dispatchCommand(sender, message);
    }

    public static void printSuccessMessage(String message) {
        MockBukkit.getOrCreateMock().getLogger().info("\u001B[32m[Test] Passed " + message + "\u001B[0m");
    }

    public static void printMessage(String message) {
        MockBukkit.getOrCreateMock().getLogger().info("\u001B[33m" + message + "\u001B[0m");
    }
}
