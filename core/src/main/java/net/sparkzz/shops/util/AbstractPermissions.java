package net.sparkzz.shops.util;

import net.sparkzz.shops.AbstractStore;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractPermissions<S> {

    private static final String OVERRIDE_NODE = "shops.admin.override";

    /**
     * Checks if the sender has the permission node
     *
     * @param sender The sender to check
     * @param node   The node to check
     * @return true if the sender has the permission node, false otherwise
     */
    protected abstract boolean hasNode(S sender, String node);

    /**
     * Gets the unique ID of the sender
     *
     * @param sender The sender to check
     * @return the sender's UUID, or empty if the sender has no identity, such as the console
     */
    protected abstract Optional<UUID> getId(S sender);

    /**
     * Checks the action's permission node only, for use before a store is known
     *
     * @return the message explaining the denial, or empty if permitted
     */
    public Optional<AbstractNotifier.CipherKey> evaluate(S sender, ShopAction action) {
        return hasNode(sender, action.getNode()) ? Optional.empty() : Optional.of(action.getDenialKey());
    }

    /**
     * Checks the action's permission node, then store ownership if the action requires it
     *
     * @param sender The sender to check
     * @param action The action to evaluate
     * @param store  The store being acted on; a null store is treated as not owned
     * @return the message explaining the denial, or empty if permitted
     */
    public Optional<AbstractNotifier.CipherKey> evaluate(S sender, ShopAction action, AbstractStore store) {
        Optional<AbstractNotifier.CipherKey> denial = evaluate(sender, action);

        if (denial.isPresent() || !action.isOwnerRequired() || isOwner(sender, store) || hasNode(sender, OVERRIDE_NODE))
            return denial;

        return Optional.of(AbstractNotifier.CipherKey.NOT_OWNER);
    }

    private boolean isOwner(S sender, AbstractStore store) {
        return store != null && getId(sender).map(id -> id.equals(store.getOwner())).orElse(false);
    }
}
