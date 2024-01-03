package net.sparkzz.shops.event;

import net.sparkzz.shops.Store;
import net.sparkzz.shops.util.Cuboid;
import net.sparkzz.shops.util.Notifiable;
import net.sparkzz.shops.util.Notifier;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for checking whether a player enters the bounds of a store
 */
public class EntranceListener extends Notifiable {

    private final Map<Player, Boolean> playerStoreStatus = new HashMap<>();

    /**
     * Checks if a player has entered or exited a shop and notifies accordingly
     *
     * @param event the PlayerMoveEvent used to determine the player's location
     */
    @Listener
    public void onPlayerMove(MoveEntityEvent event) {
        if (!(event.entity() instanceof Player))
            return;

        Player player = (Player) setAttribute("player", event.entity());
        boolean isInShop = false;

        for (Store store : Store.getStores()) {
            Cuboid cuboid = store.getCuboidLocation();

            if (cuboid == null || cuboid.getWorld() == null || !cuboid.getWorld().equals(player.world()))
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
