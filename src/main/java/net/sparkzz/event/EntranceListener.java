package net.sparkzz.event;

import net.sparkzz.shops.Store;
import net.sparkzz.util.Cuboid;
import net.sparkzz.util.Notifiable;
import net.sparkzz.util.Notifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for checking whether a player enters the bounds of a store
 */
public class EntranceListener extends Notifiable implements Listener {

    private final Map<Player, Boolean> playerStoreStatus = new HashMap<>();

    /**
     * Checks if a player has entered or exited a shop and notifies accordingly
     *
     * @param event the PlayerMoveEvent used to determine the player's location
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = (Player) setAttribute("player", event.getPlayer());
        boolean isInShop = false;

        for (Store store : Store.STORES) {
            Cuboid cuboid = store.getCuboidLocation();

            if (cuboid == null || cuboid.getWorld() == null || !cuboid.getWorld().equals(player.getWorld()))
                continue;

            if (cuboid.isPlayerWithin(player)) {
                isInShop = true;
                setAttribute("store", store);
                break;
            }
        }

        if (isInShop && !playerStoreStatus.getOrDefault(player, false)) {
            playerStoreStatus.put(player, true);
            Notifier.process(player, Notifier.CipherKey.STORE_WELCOME_MSG, getAttributes());
        } else if (!isInShop && playerStoreStatus.getOrDefault(player, true)) {
            if (playerStoreStatus.containsKey(player))
                Notifier.process(player, Notifier.CipherKey.STORE_GOODBYE_MSG, getAttributes());
            playerStoreStatus.put(player, false);
        }
    }
}
