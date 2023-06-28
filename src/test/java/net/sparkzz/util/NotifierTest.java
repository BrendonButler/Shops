package net.sparkzz.util;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Notifier Tests")
class NotifierTest {

    private static final Logger log = Logger.getLogger("Notifier");

    @Nested
    @DisplayName("MultilineBuilder Tests")
    class MultilineBuilderTest {

        private Notifier.MultilineBuilder builder;
        private static PlayerMock player;
        private static String message1, message2;

        @BeforeAll
        static void setUp() {
            printMessage("==[ TEST MultilineBuiler UTILITY ]==");
            ServerMock mock = MockBukkit.getOrCreateMock();

            player = mock.addPlayer();
            message1 = "This is a test message!";
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
        @DisplayName("Test Formatted Append")
        void testAppendf() {
            builder.appendf("%s %s", message1, message2);

            String result = builder.build();

            assertEquals(result, String.format("%s %s", message1, message2));
            printSuccessMessage("appending processed formatted message");
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