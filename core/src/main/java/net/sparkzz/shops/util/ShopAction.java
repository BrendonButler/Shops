package net.sparkzz.shops.util;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;

public enum ShopAction {
    // COMMANDS (owner not required)
    INFO("shops.cmd.shops", false, NO_PERMS_CMD),
    BROWSE("shops.cmd.browse", false, NO_PERMS_CMD),
    BUY("shops.cmd.buy", false, NO_PERMS_CMD),
    SELL("shops.cmd.sell", false, NO_PERMS_CMD),
    CREATE("shops.cmd.create", false, NO_PERMS_CMD),

    // COMMANDS (owner required)
    ADD("shops.cmd.add", true, NO_PERMS_CMD),
    REMOVE("shops.cmd.remove", true, NO_PERMS_CMD),
    UPDATE("shops.cmd.update", true, NO_PERMS_CMD),
    DELETE("shops.cmd.delete", true, NO_PERMS_CMD),
    TRANSFER("shops.cmd.transfer", true, NO_PERMS_CMD),
    DEPOSIT("shops.cmd.deposit", true, NO_PERMS_CMD),
    WITHDRAW("shops.cmd.withdraw", true, NO_PERMS_CMD),

    // CREATE SHOP (owner not required)
    CREATE_FOR_OTHER("shops.create.other-player", false, NO_PERMS_CREATE_OTHER),

    // UPDATE SHOP (owner required)
    UPDATE_INF_FUNDS("shops.update.inf-funds", true, NO_PERMS_INF_FUNDS),
    UPDATE_INF_STOCK("shops.update.inf-stock", true, NO_PERMS_INF_STOCK),
    UPDATE_LOCATION("shops.update.location", true, NO_PERMS_LOCATION),

    // OVERRIDES/LIMITS (owner not required)
    TRANSFER_IGNORE_LIMIT("shops.transfer.ignore-limit", false, NO_PERMS_CMD);

    private final String node;
    private final boolean ownerRequired;
    private final AbstractNotifier.CipherKey denialKey;

    ShopAction(String node, boolean ownerRequired, AbstractNotifier.CipherKey denialKey) {
        this.node = node;
        this.ownerRequired = ownerRequired;
        this.denialKey = denialKey;
    }

    public String getNode() {
        return node;
    }

    public boolean isOwnerRequired() {
        return ownerRequired;
    }

    public AbstractNotifier.CipherKey getDenialKey() {
        return denialKey;
    }
}
