package net.sparkzz.util;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to build and transmit messages
 *
 * @author Brendon Butler
 */
public class Notifier {
    private static final Map<CipherKey, String> messages = new HashMap<>();
    private static final String lineSeparator = System.getProperty("line.separator");

    /**
     * Composes a String from the CipherKey by either the default value or a custom value in the messages Map
     *
     * @param cipherKey the key for determining the message value
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
     * Adds an entry to the messages Map which will be used when composing messages instead of the defaults in the enum
     *
     * @param cipherKey the key to have a value mapped
     * @param message the custom message to be mapped to the CipherKey
     */
    public static void updateMessage(CipherKey cipherKey, String message) {
        messages.put(cipherKey, message);
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

    public enum CipherKey {
        ADDED_TO_STORE("§aYou have successfully added §6{material}§a to the shop!"),
        ADDED_TO_STORE_QUANTITY("§aYou have successfully added §6{quantity} §aof §6{material}§a to the shop!"),
        ADDED_MATERIAL_TO_STORE("§aYou have successfully added §6{material}§a to the shop with a buy price of §6{buy-price}§a, a sell price of §6{sell-price}§a, and a max quantity of §6{max-quantity}§a!"),
        ADDED_MATERIAL_TO_STORE_QUANTITY("§aYou have successfully added §6{quantity}§a of §6{material}§a to the shop with a buy price of §6{buy-price}§a, a sell price of §6{sell-price}§a, and a max quantity of §6{max-quantity}§a!"),
        INSUFFICIENT_AMOUNT_PLAYER("§cYou have an insufficient amount!"),
        INSUFFICIENT_FUNDS_PLAYER("§cYou have insufficient funds!"),
        INSUFFICIENT_FUNDS_STORE("§cThe Store has insufficient funds!"),
        INSUFFICIENT_INV_PLAYER("§cYou have insufficient inventory space!"),
        INSUFFICIENT_STOCK_STORE("§cThe Store has insufficient stock!"),
        INSUFFICIENT_STOCK_PLAYER("§cYou don't have enough of this item to stock the store, try leaving out the quantity and adding it later!"),
        INVALID_ARG_CNT("§cInvalid number of arguments!"),
        INVALID_MATERIAL("§cInvalid material ({material})!"),
        MATERIAL_EXISTS_STORE("§cThis material already exists in the shop, use `/shop update {material}` to update this item"),
        MATERIAL_MISSING_STORE("§cThis material doesn't currently exist in the shop, use `/shop add {material}` to add this item"),
        NO_PERMS_CMD("§cYou do not have permission to use this command!"),
        NO_PERMS_INF_STOCK("§cYou do not have permission to set infinite stock in your Shop (try using a positive quantity)!"),
        NOT_BUYING("§cThe Store is not buying any of these at this time!"),
        NOT_BUYING_ANYMORE("§cThe Store is not buying any more of these at this time!"),
        NOT_SELLING("§cThe Store is not selling any of these at this time!"),
        ONLY_PLAYERS_CMD("§cOnly players can use this command!"),
        // TODO: remove once messages contain formatting by default
        TEST_FORMAT("§a%s");

        public final String value;

        CipherKey(String value) {
            this.value = value;
        }
    }

    /**
     * The MultilineBuilder class assists in creating multi-line messages this is helpful for transaction messages if there are multiple messages to send
     */
    public static class MultilineBuilder {

        private final StringBuilder finalMessage;

        /**
         * Constructs a MultilineBuilder without any initial message
         */
        public MultilineBuilder() {
            finalMessage = new StringBuilder();
        }

        /**
         * Constructs a MultilineBuilder with an initial message
         *
         * @param message the initial message for the builder
         */
        public MultilineBuilder(String message) {
            finalMessage = new StringBuilder(message);
        }

        /**
         * Appends a message from the key
         *
         * @param key the key for determining the message value
         * @return the current instance
         */
        public MultilineBuilder append(CipherKey key) {
            return append(key.value);
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

            finalMessage.append(message);
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
            String tempMessage = String.format(key.value, args);
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
}
