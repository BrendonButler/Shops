package net.sparkzz.shops;

import be.seeseemelk.mockbukkit.MockBukkit;
import net.sparkzz.shops.util.Config;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.command.CommandSender;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestHelper {

    public static void loadConfig() {
        Path configPath = Paths.get("src", "test", "resources", "config.yml");

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().source(() ->
                new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(configPath.toFile().getAbsolutePath())
                        )
                )
        ).build();

        try {
            CommentedConfigurationNode node = loader.load();
            Config.setRootNode(node);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unLoadConfig() {
        Config.setRootNode(null);

        for (Notifier.CipherKey key : Notifier.CipherKey.values())
            Notifier.resetMessage(key);
    }

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
