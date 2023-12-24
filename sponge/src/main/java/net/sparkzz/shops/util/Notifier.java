package net.sparkzz.shops.util;

import net.kyori.adventure.text.Component;
import net.sparkzz.shops.Store;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.item.ItemType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.awt.font.TextAttribute.UNDERLINE;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.sparkzz.shops.util.AbstractNotifier.Paginator.calcPriceColWidth;

/**
 * Helper class to build and transmit messages
 *
 * @author Brendon Butler
 */
public class Notifier extends AbstractNotifier {
    private static final Map<CipherKey, String> messages = new HashMap<>();
    private static final String lineSeparator = System.getProperty("line.separator");

    /**
     * Sends the CommandCause a usage message based off invalid command usage
     *
     * @param target the target user to send a message to
     * @param args the arguments for determining the subcommand
     * @return success if handled, error if default
     */
    public static CommandResult usageSubCommand(CommandCause target, String[] args) {
        Component message = Component.text(usageSubCommand(args));
        Component defaultUsage = Component.text("/<command> [buy|sell|browse]");
        target.sendMessage((message.contains(Component.text("default")) ? defaultUsage : message));
        return CommandResult.success();
    }

    /**
     * Composes the message and sends it to the target
     *
     * @param target the target user to send a message to
     * @param cipherKey the key for determining the message value
     * @param attributes attributes that can be added to the message
     */
    public static void process(CommandCause target, CipherKey cipherKey, Map<String, Object> attributes) {
        target.sendMessage(Component.text(compose(cipherKey, attributes)));
    }

    /**
     * The MultilineBuilder class assists in creating multi-line messages this is helpful for transaction messages if there are multiple messages to send
     */
    public static class MultilineBuilder extends AbstractMultilineBuilder {

        /**
         * Constructs a MultilineBuilder without any initial message
         */
        public MultilineBuilder() {
            super();
        }

        /**
         * Constructs a MultilineBuilder without any initial message, but adds attributes
         *
         * @param attributes the attributes to be parsed in the message
         */
        public MultilineBuilder(Map<String, Object> attributes) {
            super(attributes);
        }

        /**
         * Constructs a MultilineBuilder with an initial message
         *
         * @param message the initial message for the builder
         */
        public MultilineBuilder(String message) {
            super(message);
        }

        /**
         * Constructs a MultilineBuilder with an initial message and attributes
         *
         * @param message the initial message for the builder
         * @param attributes the attributes to be parsed in the message
         */
        public MultilineBuilder(String message, Map<String, Object> attributes) {
            super(message, attributes);
        }

        /**
         * Completes the process and sends the message to the target
         *
         * @param target the target user to send a message to
         */
        public void process(CommandCause target) {
            target.sendMessage(Component.text(build()));
        }

        /**
         * Completes the process and sends each new line as an individual message
         *
         * @param target the target user to send a message to
         */
        public void processIndividual(CommandCause target) {
            String[] messages = finalMessage.toString().split(lineSeparator);

            for (String message : messages)
                target.sendMessage(Component.text(message));
        }
    }

    /**
     * Helper class for creating pages
     */
    public static class Paginator {
        private static final int pageSize = 10;

        private static int calcMaterialColWidth(Set<ItemType> materials) {
            int materialColWidth = 9;

            for (ItemType material : materials) {
                int materialWidth = material.toString().length();
                if (materialWidth > materialColWidth) {
                    materialColWidth = materialWidth;
                }
            }

            return materialColWidth;
        }

        private static Set<ItemType> getValuesForPage(List<ItemType> materials, int pageNumber) {
            int startIndex = (pageNumber - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, materials.size());

            if (startIndex >= endIndex || startIndex < 0) {
                return Collections.emptySet();
            }

            return new TreeSet<>(materials.subList(startIndex, endIndex));
        }

        /**
         * Builds the browse command page based on the input page number and store materials
         *
         * @param store the store containing the items available for sale by player and the store
         * @param pageNumber the input page number to determine what page to view
         * @return the page built containing the items in the store with the buy and sell price
         */
        public static String buildBrowsePage(Store store, int pageNumber) {
            List<ItemType> materials = store.getItems().keySet().stream().sorted().toList();
            Set<ItemType> valuesForPage = getValuesForPage(materials, pageNumber);

            if (valuesForPage.isEmpty()) return null;

            int lastPage = (int) Math.ceil(materials.size() / (double) pageSize);
            int materialColWidth = calcMaterialColWidth(valuesForPage);
            int buyColWidth = calcPriceColWidth(valuesForPage.stream().map(m -> String.valueOf(store.getAttributes(m).get("buy").doubleValue())).collect(Collectors.toSet()));

            MultilineBuilder builder = new MultilineBuilder();
            builder.appendf("%s==[ %s%s%s ]==", GRAY, DARK_AQUA, store.getName(), GRAY)
                    .appendf("%s%s| %s | %s", UNDERLINE, ("ITEM" + " ".repeat(materialColWidth - 2)), ("BUY PRICE" + " ".repeat(buyColWidth - 9)), "SELL PRICE");

            for (ItemType material : valuesForPage) {
                double buyPrice = store.getAttributes(material).get("buy").doubleValue();
                double sellPrice = store.getAttributes(material).get("sell").doubleValue();
                int materialPadding = materialColWidth - material.toString().length();
                int buyPadding = buyColWidth - String.format("%.2f", buyPrice).length();

                builder.appendf("%s%s%s: %s%s%s| %s%s",
                        DARK_GREEN, (material + " ".repeat(materialPadding + 2)), WHITE,
                        GOLD, (String.format("%.2f", buyPrice) + " ".repeat(buyPadding + 1)), WHITE,
                        GOLD, String.format("%.2f", sellPrice));
            }

            builder.appendf("Page %d of %d", pageNumber, lastPage);
            return builder.build();
        }
    }
}
