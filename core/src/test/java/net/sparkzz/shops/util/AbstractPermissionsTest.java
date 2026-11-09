package net.sparkzz.shops.util;

import net.sparkzz.shops.AbstractStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static net.sparkzz.shops.util.AbstractNotifier.CipherKey.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Permission rules")
class AbstractPermissionsTest {

    private static final String OVERRIDE = "shops.admin.override";
    private static final UUID OWNER = UUID.randomUUID();
    private static final UUID OTHER = UUID.randomUUID();

    private record FakeSender(UUID id, Set<String> nodes) {}

    private static class FakeStore extends AbstractStore {
        FakeStore(UUID owner) {
            super("Test", owner);
        }
    }

    private static class FakePermissions extends AbstractPermissions<FakeSender> {
        @Override
        protected boolean hasNode(FakeSender sender, String node) {
            return sender.nodes().contains(node);
        }

        @Override
        protected Optional<UUID> getId(FakeSender sender) {
            return Optional.ofNullable(sender.id());
        }
    }

    private final FakePermissions util = new FakePermissions();
    private final AbstractStore store = new FakeStore(OWNER);
    private final String addNode = ShopAction.ADD.getNode();

    @Test
    void nodeOnly_missingNode_usesActionDenialKey() {
        FakeSender sender = new FakeSender(OWNER, Set.of());

        assertEquals(Optional.of(NO_PERMS_INF_STOCK), util.evaluate(sender, ShopAction.UPDATE_INF_STOCK));
    }

    @Test
    void store_owner_isPermitted() {
        FakeSender sender = new FakeSender(OWNER, Set.of(addNode));

        assertEquals(Optional.empty(), util.evaluate(sender, ShopAction.ADD, store));
    }

    @Test
    void store_nonOwner_isDenied() {
        FakeSender sender = new FakeSender(OTHER, Set.of(addNode));

        assertEquals(Optional.of(NOT_OWNER), util.evaluate(sender, ShopAction.ADD, store));
    }

    @Test
    void store_nonOwnerWithOverride_isPermitted() {
        FakeSender sender = new FakeSender(OTHER, Set.of(addNode, OVERRIDE));

        assertEquals(Optional.empty(), util.evaluate(sender, ShopAction.ADD, store));
    }

    @Test
    void store_overrideWithoutNode_isDenied() {
        FakeSender sender = new FakeSender(OTHER, Set.of(OVERRIDE));

        assertEquals(Optional.of(NO_PERMS_CMD), util.evaluate(sender, ShopAction.ADD, store));
    }

    @Test
    void store_ownerlessStore_isDenied() {
        FakeSender sender = new FakeSender(OTHER, Set.of(addNode));

        assertEquals(Optional.of(NOT_OWNER), util.evaluate(sender, ShopAction.ADD, new FakeStore(null)));
    }

    @Test
    void store_senderWithoutId_isDenied() {
        FakeSender sender = new FakeSender(null, Set.of(addNode));

        assertEquals(Optional.of(NOT_OWNER), util.evaluate(sender, ShopAction.ADD, store));
    }

    @Test
    void store_nullStore_isDenied() {
        FakeSender sender = new FakeSender(OWNER, Set.of(addNode));

        assertEquals(Optional.of(NOT_OWNER), util.evaluate(sender, ShopAction.ADD, null));
    }

    @Test
    void store_nullStoreWithOverride_isPermitted() {
        FakeSender sender = new FakeSender(OTHER, Set.of(addNode, OVERRIDE));

        assertEquals(Optional.empty(), util.evaluate(sender, ShopAction.ADD, null));
    }

    @Test
    void nodeOnly_hasNode_isPermittedWithoutOwnershipCheck() {
        FakeSender sender = new FakeSender(OTHER, Set.of(addNode));

        assertEquals(Optional.empty(), util.evaluate(sender, ShopAction.ADD));
    }

    @Test
    void nodeOnly_missingNode_isDenied() {
        FakeSender sender = new FakeSender(OWNER, Set.of());

        assertEquals(Optional.of(NO_PERMS_CMD), util.evaluate(sender, ShopAction.ADD));
    }

    @Test
    void store_ownerlessStoreWithOverride_isPermitted() {
        FakeSender sender = new FakeSender(OTHER, Set.of(addNode, OVERRIDE));

        assertEquals(Optional.empty(), util.evaluate(sender, ShopAction.ADD, new FakeStore(null)));
    }

    @Test
    void store_nonOwnerOnActionWithoutOwnership_isPermitted() {
        FakeSender sender = new FakeSender(OTHER, Set.of(ShopAction.BROWSE.getNode()));

        assertEquals(Optional.empty(), util.evaluate(sender, ShopAction.BROWSE, store));
    }
}