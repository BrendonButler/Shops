package net.sparkzz.shops;

import net.milkbowl.vault.economy.Economy;
import net.sparkzz.shops.mocks.FakeEconomy;
import net.sparkzz.shops.util.Config;
import net.sparkzz.shops.util.Notifier;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class TestHelper {

    private static final Logger log = Logger.getLogger("ShopsTest");

    /**
     * Starts a fresh mock server with a mock Vault plugin and an in-memory economy, then loads Shops and the
     * test config. Pair with {@link #stopServer()}.
     *
     * @return the mock server
     */
    public static ServerMock startServer() {
        ServerMock server = MockBukkit.mock();
        Plugin vault = MockBukkit.createMockPlugin("Vault");

        server.getServicesManager().register(Economy.class, new FakeEconomy(), vault, ServicePriority.Normal);
        MockBukkit.load(Shops.class);
        loadConfig();

        return server;
    }

    /**
     * Stops the mock server and resets all static state so the next test class starts clean
     */
    public static void stopServer() {
        MockBukkit.unmock();
        unloadConfig();
        Store.STORES.clear();
        Store.DEFAULT_STORES.clear();
        Shops.setEconomy(null);
    }

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

    public static void unloadConfig() {
        Config.setRootNode(null);

        for (Notifier.CipherKey key : Notifier.CipherKey.values())
            Notifier.resetMessage(key);
    }

    /**
     * Runs an action while the player has the given permission explicitly denied. Used for permissions that
     * plugin.yml grants to everyone by default.
     *
     * @param player the player to deny the permission for
     * @param permission the permission to deny
     * @param action the action to run while the permission is denied
     */
    public static void withoutPermission(PlayerMock player, String permission, Runnable action) {
        Plugin shops = MockBukkit.getMock().getPluginManager().getPlugin("Shops");
        PermissionAttachment attachment = player.addAttachment(shops, permission, false);

        try {
            action.run();
        } finally {
            player.removeAttachment(attachment);
        }
    }

    /**
     * Sets a player's economy balance to an exact amount, regardless of what it was before
     *
     * @param player the player whose balance to set
     * @param amount the new balance
     */
    public static void setBalance(OfflinePlayer player, double amount) {
        Economy economy = Shops.getEconomy();
        economy.withdrawPlayer(player, economy.getBalance(player));
        economy.depositPlayer(player, amount);
    }

    public static void performCommand(CommandSender sender, String message) {
        assert MockBukkit.getMock() != null;
        MockBukkit.getMock().dispatchCommand(sender, message);
    }

    public static void printSuccessMessage(String message) {
        log.info("\u001B[32m[Test] Passed " + message + "\u001B[0m");
    }

    public static void printMessage(String message) {
        log.info("\u001B[33m" + message + "\u001B[0m");
    }
}
