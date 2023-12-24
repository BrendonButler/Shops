package net.sparkzz.shops.util;

import net.sparkzz.shops.Store;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bukkit.ChatColor.*;

/**
 * Helper class to build and transmit messages
 *
 * @author Brendon Butler
 */
public class Notifier extends AbstractNotifier {
    private static final Map<CipherKey, String> messages = new HashMap<>();
    private static final String lineSeparator = System.getProperty("line.separator");

    /**
     * Sends the CommandSender a usage message based off invalid command usage
     *
     * @param target the target user to send a message to
     * @param args the arguments for determining the subcommand
     * @return true if handled, false if default
     */
    public static boolean usageSubCommand(CommandSender target, String[] args) {
        String message = "/shop ";

        message += switch (args[0]) {
            case "add" -> (args.length < 3 ? "add <material> [<quantity>|all]" : "add <material> <customer-buy-price> <customer-sell-price> <max-quantity> [<quantity>|all]");
            case "remove" -> "remove <material> [<quantity>|all]";
            case "update" -> "update [<material>|<shop-name>|<infinite-funds>|<infinite-stock>]";
            case "buy" -> "buy <material> [<quantity>]";
            case "sell" -> "sell <material> [<quantity>|all]";
            case "create" -> "create <name>";
            case "delete" -> "delete [<name>|<uuid>|<name>~<uuid>]";
            case "transfer" -> "transfer [<name>|<uuid>|<name>~<uuid>] <player>";
            case "deposit" -> "deposit <amount>";
            case "withdraw" -> "withdraw <amount>";
            default -> "default";
        };

        if (message.contains("default")) return false;
        target.sendMessage(message);
        return true;
    }

    /**
     * Composes a String from the CipherKey by either the default value or a custom value in the messages Map
     *
     * @param cipherKey the key for determining the message value
     * @param attributes the attributes to be attached to the input string
     * @return the custom message if it exists or the default message
     */
    public static String compose(CipherKey cipherKey, Map<String, Object> attributes) {
        return format((!messages.isEmpty() && messages.containsKey(cipherKey)) ? messages.get(cipherKey) : cipherKey.value, attributes);
    }

    /**
     * Formats an input string containing attributes within curly braces
     *
     * @param input the input string to have attributes attached
     * @param attributes the attributes to be attached to the input string
     * @return the modified input string
     */
    public static String format(String input, @Nullable Map<String, Object> attributes) {
        if (attributes != null && !attributes.isEmpty()) {
            Pattern pattern = Pattern.compile("\\{([^}]+)}");
            Matcher matcher = pattern.matcher(input);
            StringBuilder builder = new StringBuilder();

            while (matcher.find()) {
                String placeholder = matcher.group(1);
                Object replacement = attributes.get(placeholder);
                matcher.appendReplacement(builder, (replacement == null ? "{" + placeholder + "}" : replacement.toString()));
            }

            matcher.appendTail(builder);
            input = builder.toString();
        }

        return input;
    }

    /**
     * Loads custom messages from the config to replace default messages in CipherKey
     */
    public static void loadCustomMessages() {
        if (Config.getRootNode() == null)
            return;

        CipherKey[] key = CipherKey.values();

        for (CipherKey cipherKey : key) {
            String message = Config.getMessage(cipherKey);

            if (message != null && !message.isEmpty())
                updateMessage(cipherKey, message);
        }
    }

    /**
     * Composes the message and sends it to the target
     *
     * @param target the target user to send a message to
     * @param cipherKey the key for determining the message value
     * @param attributes attributes that can be added to the message
     */
    public static void process(CommandSender target, CipherKey cipherKey, Map<String, Object> attributes) {
        target.sendMessage(compose(cipherKey, attributes));
    }

    /**
     * Resets the custom message to the default by deleting it from the messages Map
     *
     * @param cipherKey the key for determining the message value
     */
    public static void resetMessage(CipherKey cipherKey) {
        messages.remove(cipherKey);
    }

    /**
     * Adds an entry to the messages Map which will be used when composing messages instead of the defaults in the enum
     *
     * @param cipherKey the key to have a value mapped
     * @param message the custom message to be mapped to the CipherKey
     */
    public static void updateMessage(CipherKey cipherKey, String message) {
        messages.put(cipherKey, message);
    }

    /**
     * The MultilineBuilder class assists in creating multi-line messages this is helpful for transaction messages if there are multiple messages to send
     */
    public static class MultilineBuilder {

        private final StringBuilder finalMessage;
        private final Map<String, Object> attributes;

        /**
         * Constructs a MultilineBuilder without any initial message
         */
        public MultilineBuilder() {
            finalMessage = new StringBuilder();
            attributes = null;
        }

        /**
         * Constructs a MultilineBuilder without any initial message, but adds attributes
         *
         * @param attributes the attributes to be parsed in the message
         */
        public MultilineBuilder(Map<String, Object> attributes) {
            finalMessage = new StringBuilder();
            this.attributes = attributes;
        }

        /**
         * Constructs a MultilineBuilder with an initial message
         *
         * @param message the initial message for the builder
         */
        public MultilineBuilder(String message) {
            finalMessage = new StringBuilder(message);
            attributes = null;
        }

        /**
         * Constructs a MultilineBuilder with an initial message and attributes
         *
         * @param message the initial message for the builder
         * @param attributes the attributes to be parsed in the message
         */
        public MultilineBuilder(String message, Map<String, Object> attributes) {
            finalMessage = new StringBuilder(format(message, attributes));
            this.attributes = attributes;
        }

        /**
         * Appends a message from the key
         *
         * @param key the key for determining the message value
         * @return the current instance
         */
        public MultilineBuilder append(CipherKey key) {
            return append(compose(key, attributes));
        }

        /**
         * Appends a message to a new line of the builder
         *
         * @param message the message to be appended
         * @return the current instance
         */
        public MultilineBuilder append(String message) {
            if (!finalMessage.isEmpty())
                finalMessage.append(lineSeparator);

            finalMessage.append(format(message, attributes));
            return this;
        }

        /**
         * Appends a message with formatting from a key
         *
         * @param key the key for determining the message value
         * @param args the arguments to be formatted in the message
         * @return the current instance
         */
        public MultilineBuilder appendf(CipherKey key, @Nullable Object... args) {
            String tempMessage = String.format(compose(key, attributes), args);
            return append(tempMessage);
        }

        /**
         * Appends a message with formatting
         *
         * @param message the message to be appended
         * @param args the arguments to be formatted in the message
         * @return the current instance
         */
        public MultilineBuilder appendf(@NotNull String message, @Nullable Object... args) {
            String tempMessage = String.format(message, args);
            return append(tempMessage);
        }

        /**
         * Builds the mutli-line message to a string
         *
         * @return the finalMessage as a completed string
         */
        public String build() {
            return finalMessage.toString();
        }

        /**
         * Completes the process and sends the message to the target
         *
         * @param target the target user to send a message to
         */
        public void process(CommandSender target) {
            target.sendMessage(build());
        }

        /**
         * Completes the process and sends each new line as an individual message
         *
         * @param target the target user to send a message to
         */
        public void processIndividual(CommandSender target) {
            String[] messages = finalMessage.toString().split(lineSeparator);

            for (String message : messages)
                target.sendMessage(message);
        }
    }

    /**
     * Helper class for creating pages
     */
    public static class Paginator {
        private static final int pageSize = 10;

        private static int calcMaterialColWidth(Set<Material> materials) {
            int materialColWidth = 9;

            for (Material material : materials) {
                int materialWidth = material.toString().length();
                if (materialWidth > materialColWidth) {
                    materialColWidth = materialWidth;
                }
            }

            return materialColWidth;
        }

        private static int calcPriceColWidth(Set<String> values) {
            int priceColWidth = 9;

            for (String value : values) {
                int priceWidth = value.length();
                if (priceWidth > priceColWidth) {
                    priceColWidth = priceWidth;
                }
            }

            return priceColWidth;
        }

        private static Set<Material> getValuesForPage(List<Material> materials, int pageNumber) {
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
            List<Material> materials = store.getItems().keySet().stream().sorted().toList();
            Set<Material> valuesForPage = getValuesForPage(materials, pageNumber);

            if (valuesForPage.isEmpty()) return null;

            int lastPage = (int) Math.ceil(materials.size() / (double) pageSize);
            int materialColWidth = calcMaterialColWidth(valuesForPage);
            int buyColWidth = calcPriceColWidth(valuesForPage.stream().map(m -> String.valueOf(store.getAttributes(m).get("buy").doubleValue())).collect(Collectors.toSet()));

            MultilineBuilder builder = new MultilineBuilder();
            builder.appendf("%s==[ %s%s%s ]==", GRAY, DARK_AQUA, store.getName(), GRAY)
                    .appendf("%s%s| %s | %s", UNDERLINE, ("ITEM" + " ".repeat(materialColWidth - 2)), ("BUY PRICE" + " ".repeat(buyColWidth - 9)), "SELL PRICE");

            for (Material material : valuesForPage) {
                double buyPrice = store.getAttributes(material).get("buy").doubleValue();
                double sellPrice = store.getAttributes(material).get("sell").doubleValue();
                int materialPadding = materialColWidth - material.toString().length();
                int buyPadding = buyColWidth - String.format("%.2f", buyPrice).length();

                builder.appendf("%s%s%s: %s%s%s| %s%s",
                        DARK_GREEN, (material + " ".repeat(materialPadding + 2)), RESET,
                        GOLD, (String.format("%.2f", buyPrice) + " ".repeat(buyPadding + 1)), RESET,
                        GOLD, String.format("%.2f", sellPrice));
            }

            builder.appendf("Page %d of %d", pageNumber, lastPage);
            return builder.build();
        }
    }
}
