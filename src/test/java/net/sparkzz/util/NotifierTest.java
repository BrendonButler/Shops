package net.sparkzz.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Notifier Tests")
class NotifierTest {

    private static final Logger log = Logger.getLogger("Notifier");

    private static PlayerMock player;
    private static String message1;

    @BeforeAll
    static void setUp() {
        printMessage("==[ TEST Notifier UTILITY ]==");
        ServerMock mock = MockBukkit.getOrCreateMock();

        player = mock.addPlayer();
        message1 = "This is a test message!";
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

        private static String message2;

        private Notifier.MultilineBuilder builder;

        @BeforeAll
        static void setUp() {
            printMessage("==[ TEST MultilineBuilder UTILITY ]==");

            message2 = "This should be on a new line!";
        }

        @BeforeEach
        void setUpEach() {
            builder = new Notifier.MultilineBuilder();
        }

        @Test
        @DisplayName("Test Append")
        void testAppend() {
            builder.append(message1);
            builder.append(message2);

            String result = builder.build();

            assertEquals(result, String.format("%s%s%s", message1, System.getProperty("line.separator"), message2));
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
        @DisplayName("Test formatted Append")
        void testAppendf() {
            builder.appendf("%s %s", message1, message2);

            String result = builder.build();

            assertEquals(result, String.format("%s %s", message1, message2));
            printSuccessMessage("appending processed formatted message");
        }

        @Test
        @DisplayName("Test formatted Append from key")
        void testAppendf_FromKey() {
            builder.appendf(Notifier.CipherKey.TEST_FORMAT, message1);

            String result = builder.build();
            String expected = String.format("§a%s", message1);

            assertEquals(expected, result);
            printSuccessMessage("appending processed formatted message from key");
        }

        @Test
        @DisplayName("Test Processing message to Player")
        void testProcess() {
            builder.append(message1).append(message2).process(player);

            String result = builder.build();

            assertEquals(result, player.nextMessage());
            printSuccessMessage("processing message to player");
        }

        @Test
        @DisplayName("Test Processing Individual messages to Player")
        void testProcessIndividual() {
            builder.append(message1).append(message2).processIndividual(player);

            assertEquals(message1, player.nextMessage());
            assertEquals(message2, player.nextMessage());
            printSuccessMessage("processing individual messages to Player");
        }
    }

    private static void printSuccessMessage(String message) {
        log.info("[Shops] \u001B[32m[Notifier Test] Passed " + message + "\u001B[0m");
    }

    private static void printMessage(String message) {
        log.info("[Shops] \u001B[33m" + message + "\u001B[0m");
    }
}