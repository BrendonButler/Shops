package net.sparkzz.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.sparkzz.shops.Store;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Notifier Tests")
class NotifierTest {

    private static final Logger log = Logger.getLogger("Notifier");

    private static PlayerMock player;
    private static String message1, message2;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST Notifier UTILITY ]==");
        ServerMock mock = MockBukkit.getOrCreateMock();

        player = mock.addPlayer();
        message1 = "This is a test message!";
        message2 = "Test the attributes: {player} is {mood}";
    }

    @Test
    @DisplayName("Test Format empty attributes")
    void testFormat_EmptyAttributes() {
        String expected = "Test {placeholder} with empty attributes";
        String result = Notifier.format(expected, new HashMap<>());

        assertEquals(expected, result);
        printSuccessMessage("formatted message with empty attributes");
    }

    @Test
    @DisplayName("Test Format null attributes")
    void testFormat_NullAttributes() {
        String expected = "Test {placeholder} with null attributes";
        String result = Notifier.format(expected, null);

        assertEquals(expected, result);
        printSuccessMessage("formatted message with null attributes");
    }

    @Test
    @DisplayName("Test Format with no replacement attribute")
    void testFormat_WithNoReplacementAttribute() {
        String expected = "Test {placeholder} with no replacement attribute";
        String result = Notifier.format(expected, Map.ofEntries(entry("test", "test")));

        assertEquals(expected, result);
        printSuccessMessage("formatted message with no replacement attribute");
    }

    @Test
    @DisplayName("Test Update message")
    void testUpdate() {
        Notifier.CipherKey key = Notifier.CipherKey.NO_PERMS_CMD;
        Notifier.updateMessage(key, message1);
        String updatedMessage = Notifier.compose(key, null);
        Notifier.resetMessage(key);

        assertEquals(message1, updatedMessage);
        printSuccessMessage("processing message to player");
    }

    @Test
    @DisplayName("Test Processing message from CipherKey to player")
    void testProcess() {
        Notifier.CipherKey key = Notifier.CipherKey.NO_PERMS_CMD;
        String result = Notifier.compose(key, null);

        Notifier.process(player, key, null);
        assertEquals(result, player.nextMessage());
        printSuccessMessage("processing message to player");
    }

    @Test
    @DisplayName("Test Process SubCommand usage messages to sender")
    void testUsageSubCommand() {
        boolean addReturnValue = Notifier.usageSubCommand(player, new String[]{"add"});
        boolean addReturnValueMoreArgs = Notifier.usageSubCommand(player, new String[]{"add", "1", "1", "1"});
        boolean removeReturnValue = Notifier.usageSubCommand(player, new String[]{"remove"});
        boolean updateReturnValue = Notifier.usageSubCommand(player, new String[]{"update"});
        boolean buyReturnValue = Notifier.usageSubCommand(player, new String[]{"buy"});
        boolean sellReturnValue = Notifier.usageSubCommand(player, new String[]{"sell"});
        boolean createReturnValue = Notifier.usageSubCommand(player, new String[]{"create"});
        boolean deleteReturnValue = Notifier.usageSubCommand(player, new String[]{"delete"});
        boolean transferReturnValue = Notifier.usageSubCommand(player, new String[]{"transfer"});
        boolean depositReturnValue = Notifier.usageSubCommand(player, new String[]{"deposit"});
        boolean withdrawReturnValue = Notifier.usageSubCommand(player, new String[]{"withdraw"});
        boolean defaultReturnValue = Notifier.usageSubCommand(player, new String[]{""});

        assertTrue(addReturnValue);
        assertTrue(addReturnValueMoreArgs);
        assertTrue(removeReturnValue);
        assertTrue(updateReturnValue);
        assertTrue(buyReturnValue);
        assertTrue(sellReturnValue);
        assertTrue(createReturnValue);
        assertTrue(deleteReturnValue);
        assertTrue(transferReturnValue);
        assertTrue(depositReturnValue);
        assertTrue(withdrawReturnValue);
        assertFalse(defaultReturnValue);

        assertEquals("/shop add <material> [<quantity>|all]", player.nextMessage());
        assertEquals("/shop add <material> <customer-buy-price> <customer-sell-price> <max-quantity> [<quantity>|all]", player.nextMessage());
        assertEquals("/shop remove <material> [<quantity>|all]", player.nextMessage());
        assertEquals("/shop update [<material>|<shop-name>|<infinite-funds>|<infinite-stock>]", player.nextMessage());
        assertEquals("/shop buy <material> [<quantity>]", player.nextMessage());
        assertEquals("/shop sell <material> [<quantity>|all]", player.nextMessage());
        assertEquals("/shop create <name>", player.nextMessage());
        assertEquals("/shop delete [<name>|<uuid>|<name>~<uuid>]", player.nextMessage());
        assertEquals("/shop transfer [<name>|<uuid>|<name>~<uuid>] <player>", player.nextMessage());
        assertEquals("/shop deposit <amount>", player.nextMessage());
        assertEquals("/shop withdraw <amount>", player.nextMessage());

        printSuccessMessage("processing subcommand usage messages to player");
    }

    @Test
    @DisplayName("Test Composing message to a String from default with non-empty custom messages")
    void testCompose() {
        Notifier.CipherKey key = Notifier.CipherKey.NO_PERMS_CMD;

        String altString = "Test";
        Notifier.updateMessage(Notifier.CipherKey.ONLY_PLAYERS_CMD, altString);

        String result = Notifier.compose(key, null);

        assertEquals(result, Notifier.CipherKey.NO_PERMS_CMD.value);
        printSuccessMessage("composing message to a String using default with non-empty custom messages");
    }

    @Nested
    @DisplayName("MultilineBuilder Tests")
    class MultilineBuilderTest {

        private static String message3;

        private Notifier.MultilineBuilder builder;

        @BeforeAll
        static void setUp() {
            printMessage("==[ TEST MultilineBuilder UTILITY ]==");

            message3 = "This should be on a new line!";
        }

        @BeforeEach
        void setUpEach() {
            builder = new Notifier.MultilineBuilder();
        }

        @Test
        @DisplayName("Test Append")
        void testAppend() {
            builder.append(message1);
            builder.append(message3);

            String result = builder.build();

            assertEquals(result, String.format("%s%s%s", message1, System.getProperty("line.separator"), message3));
            printSuccessMessage("appending processed message");
        }

        @Test
        @DisplayName("Test Append from key")
        void testAppend_FromKey() {
            builder.append(Notifier.CipherKey.INSUFFICIENT_FUNDS_PLAYER);
            builder.append(Notifier.CipherKey.INSUFFICIENT_AMOUNT_PLAYER);

            String result = builder.build();

            assertEquals(result, String.format("%s%s%s", Notifier.CipherKey.INSUFFICIENT_FUNDS_PLAYER.value, System.getProperty("line.separator"), Notifier.CipherKey.INSUFFICIENT_AMOUNT_PLAYER.value));
            printSuccessMessage("appending processed message from key");
        }

        @Test
        @DisplayName("Test MultilineBuilder constructor")
        void testConstructor() {
            Notifier.MultilineBuilder builder = new Notifier.MultilineBuilder(message1);

            assertEquals(message1, builder.build());
            printSuccessMessage("MultilineBuilder constructor with default message");
        }

        @Test
        @DisplayName("Test MultilineBuilder constructor with default message and attributes")
        void testConstructor_StarterMessageAndAttributes() {
            Map<String, Object> attributes = Map.ofEntries(
                    entry("player", player.getName()),
                    entry("mood", "Happy")
            );
            Notifier.MultilineBuilder builder = new Notifier.MultilineBuilder(message2, attributes);
            builder.append(message3);

            assertEquals(String.format("Test the attributes: %s is %s%n%s", player.getName(), "Happy", message3), builder.build());
            printSuccessMessage("MultilineBuilder constructor with default message and attributes");
        }

        @Test
        @DisplayName("Test formatted Append")
        void testAppendf() {
            builder.appendf("%s %s", message1, message3);

            String result = builder.build();

            assertEquals(result, String.format("%s %s", message1, message3));
            printSuccessMessage("appending processed formatted message");
        }

        @Test
        @DisplayName("Test formatted Append from key")
        void testAppendf_FromKey() {
            builder.appendf(Notifier.CipherKey.NO_PERMS_CMD, message1);

            String result = builder.build();

            assertEquals("§cYou do not have permission to use this command!", result);
            printSuccessMessage("appending processed formatted message from key");
        }

        @Test
        @DisplayName("Test Processing message to Player")
        void testProcess() {
            builder.append(message1).append(message3).process(player);

            String result = builder.build();

            assertEquals(result, player.nextMessage());
            printSuccessMessage("processing message to player");
        }

        @Test
        @DisplayName("Test Processing Individual messages to Player")
        void testProcessIndividual() {
            builder.append(message1).append(message3).processIndividual(player);

            assertEquals(message1, player.nextMessage());
            assertEquals(message3, player.nextMessage());
            printSuccessMessage("processing individual messages to Player");
        }
    }

    @Nested
    @DisplayName("Paginator Tests")
    class PaginatorTest {

        private static Store store;

        @BeforeAll
        static void setUp() {
            printMessage("==[ TEST Paginator UTILITY ]==");

            store = new Store("TestStore");
            store.addItem(Material.EMERALD, 3, 64, 24.5, 12);
            store.addItem(Material.ACACIA_LOG, 2018, -1, 2, 1);
            store.addItem(Material.ITEM_FRAME, 1, 24, 13, 10);
            store.addItem(Material.OBSIDIAN, 100, 4, 2, 1);
            store.addItem(Material.ACACIA_SIGN, 0, -1, 0, 0);
            store.addItem(Material.IRON_AXE, 4, 10, 10, 5);
            store.addItem(Material.COPPER_BLOCK, 10000, -1, 2, 1);
            store.addItem(Material.CHARCOAL, 10, 128, .5, .1);
            store.addItem(Material.BEEF, 1000, 2048, 4, 1.5);
            store.addItem(Material.BUCKET, 2, 12, 10, 2.5);
            store.addItem(Material.SPRUCE_LOG, 40, 64, 4, 2);
            store.addItem(Material.STICK, 12800, -1, 0.25, 0.1);
        }

        @Test
        @DisplayName("Test Building Browse Page")
        void testBuildBrowsePage() {
            String result = Notifier.Paginator.buildBrowsePage(store, 2);
            String expected = """
                    §7==[ §3TestStore§7 ]==
                    §nITEM        | BUY PRICE | SELL PRICE
                    §2BEEF        §r: §64.00      §r| §61.50
                    §2ITEM_FRAME  §r: §613.00     §r| §610.00
                    Page 2 of 2""";

            assertEquals(expected, result);
            printSuccessMessage("processing page of pagination");
        }
    }

    private static void printSuccessMessage(String message) {
        log.info("[Shops] \u001B[32m[Notifier Test] Passed " + message + "\u001B[0m");
    }

    private static void printMessage(String message) {
        log.info("[Shops] \u001B[33m" + message + "\u001B[0m");
    }
}