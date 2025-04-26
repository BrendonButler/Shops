package net.sparkzz.shops.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to build and transmit messages
 *
 * @author Brendon Butler
 */
public class AbstractNotifier {
    private static final Map<CipherKey, String> messages = new HashMap<>();
    private static final String lineSeparator = System.getProperty("line.separator");

    /**
     * Sends the CommandSender a usage message based off invalid command usage
     *
     * @param args the arguments for determining the subcommand
     * @return usage message
     */
    public static String usageSubCommand(String[] args) {
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

        return message;
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
        if (AbstractConfig.getRootNode() == null)
            return;

        CipherKey[] key = CipherKey.values();

        for (CipherKey cipherKey : key) {
            String message = AbstractConfig.getMessage(cipherKey);

            if (message != null && !message.isEmpty())
                updateMessage(cipherKey, message);
        }
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
     * CipherKeys in this context are the keys with default values and wildcards to be processed
     */
    public enum CipherKey {
        ADD_SUCCESS("§aYou have successfully added §6{material}§a to the store!"),
        ADD_SUCCESS_QUANTITY("§aYou have successfully added §6{quantity} §aof §6{material}§a to the store!"),
        ADDED_MATERIAL_TO_STORE("§aYou have successfully added §6{material}§a to the store with a buy price of §6{buy-price}§a, a sell price of §6{sell-price}§a, and a max quantity of §6{max-quantity}§a!"),
        ADDED_MATERIAL_TO_STORE_QUANTITY("§aYou have successfully added §6{quantity}§a of §6{material}§a to the store with a buy price of §6{buy-price}§a, a sell price of §6{sell-price}§a, and a max quantity of §6{max-quantity}§a!"),
        PRICE("§9Price: §a{cost}"),
        BUY_SUCCESS("§aSuccess! You have purchased §6{quantity}§a of §6{material}§a for §6{cost}§a."),
        DEPOSIT_INF_FUNDS("§aThis store has infinite funds, depositing funds isn't necessary!"),
        DEPOSIT_SUCCESS("§aYou have successfully deposited §6{amount}§a to the store!"),
        INSUFFICIENT_AMOUNT_PLAYER("§cYou have an insufficient amount!"),
        INSUFFICIENT_FUNDS_PLAYER("§cYou have insufficient funds!"),
        INSUFFICIENT_FUNDS_STORE("§cThe store has insufficient funds!"),
        INSUFFICIENT_INV_PLAYER("§cYou have insufficient inventory space!"),
        INSUFFICIENT_INV_STORE("§cThe store currently doesn't have enough §6{material}§c!"),
        INSUFFICIENT_STOCK_STORE("§cThe store has insufficient stock!"),
        INSUFFICIENT_STOCK_PLAYER("§cYou don't have enough of this item to stock the store, try leaving out the quantity and adding it later!"),
        INVALID_ARG_CNT("§cInvalid number of arguments!"),
        INVALID_PAGE_NUM("§cInvalid page number!"),
        INVALID_QUANTITY("§cInvalid quantity ({quantity})!"),
        INVALID_MATERIAL("§cInvalid material ({material})!"),
        MATERIAL_EXISTS_STORE("§cThis material already exists in the store, use `/shop update {material}` to update this item"),
        MATERIAL_MISSING_STORE("§cThis material doesn't currently exist in the store, use `/shop add {material}` to add this item"),
        NO_PERMS_CMD("§cYou do not have permission to use this command!"),
        NO_PERMS_CREATE_OTHER("§cYou do not have permission to create stores for other players!"),
        NO_PERMS_INF_FUNDS("§cYou do not have permission to set infinite funds in your store!"),
        NO_PERMS_INF_STOCK("§cYou do not have permission to set infinite stock in your store!"),
        NO_PERMS_LOCATION("§cYou do not have permission to set the location of your store!"),
        NO_STORE_FOUND("§cYou are not currently in a store!"),
        NOT_BUYING("§cThe store is not buying any of these at this time!"),
        NOT_BUYING_ANYMORE("§cThe Store is not buying any more of these at this time!"),
        NOT_SELLING("§cThe store is not selling any of these at this time!"),
        NOT_OWNER("§cYou are not the owner of this store, you cannot perform this command!"),
        ONLY_PLAYERS_CMD("§cOnly players can use this command!"),
        PLAYER_NOT_FOUND("§cPlayer ({target}) not found!"),
        REMOVE_INSUFFICIENT_INV_PLAYER("§cYou don't have enough inventory space to remove §6{material}§c from your store, please try specifying a quantity then removing once the store quantity is lesser!"),
        REMOVE_SUCCESS("§aYou have successfully removed §6{material}§a from the store!"),
        REMOVE_SUCCESS_QUANTITY("§aYou have successfully removed §6{quantity} §aof §6{material}§a to the store!"),
        SELL_SUCCESS("§aSuccess! You have sold §6{quantity}§a of §6{material}§a for §6{cost}§a."),
        STORE_CREATE_SUCCESS("§aYou have successfully created §6{store}§a!"),
        STORE_CREATE_SUCCESS_OTHER_PLAYER("§aYou have successfully created §6{store}§a for §6{target}§a!"),
        STORE_CREATE_FAIL_MAX_DIMS("§cYou can't create a store that large!§f Maximum dimensions: ({limit-max-x}, {limit-max-y}, {limit-max-z})."),
        STORE_CREATE_FAIL_MIN_DIMS("§cYou can't create a store that small!§f Minimum dimensions: ({limit-min-x}, {limit-min-y}, {limit-min-z})."),
        STORE_CREATE_FAIL_MAX_STORES("§cYou can't create any more stores!§f Maximum stores: {max-stores}."),
        STORE_CREATE_FAIL_MAX_VOL("§cYou can't create a store that large!§f Maximum volume: {limit-max-vol}."),
        STORE_CREATE_FAIL_MIN_VOL("§cYou can't create a store that small!§f Minimum volume: {limit-min-vol}."),
        STORE_CREATE_FAIL_OFFLIMITS("§cYou can't create a store within this area (off limits)!"),
        STORE_CREATE_FAIL_OVERLAPS("§cYou can't create a store within this area (intersects another store)!"),
        STORE_DELETE_FAIL("§cSomething went wrong when attempting to delete the store!"),
        STORE_DELETE_SUCCESS("§aYou have successfully deleted §6{store}§a!"),
        STORE_DELETE_INSUFFICIENT_INV_PLAYER("§cYou don't have enough inventory space to delete the store, please try removing items first or use the '-f' flag to ignore inventory!"),
        STORE_GOODBYE_MSG("§9We hope to see you again!"),
        STORE_MULTI_MATCH("§cMultiple stores matched, please specify the store's UUID!"),
        STORE_NO_ITEMS("§cThis store is currently empty!"),
        STORE_NO_STORE_FOUND("§cCould not find a store with the name and/or UUID of: §6{store}§c!"),
        STORE_TRANSFER_FAIL_MAX_STORES("§c{target} can't have any more stores!§f Maximum stores: {max-stores}."),
        STORE_TRANSFER_SUCCESS("§aYou have successfully transferred §6{store}§a to player §6{target}§a!"),
        STORE_UPDATE_SUCCESS("§aYou have successfully updated §6{arg1}§a to §6{arg2}§a in {store}!"),
        STORE_UPDATE_SUCCESS_2("§aYou have successfully updated §6{arg2}§a to §6{arg3}§a in {store}!"),
        STORE_UPDATE_SUCCESS_LOCATION("§aYou have successfully updated the location of {store} to ({x1}, {y1}, {z1}) ({x2}, {y2}, {z2}) in {world}!"),
        STORE_UPDATE_NO_STOCK("§cPlease ensure there is no stock in the store for this item and try again!"),
        STORE_WELCOME_MSG("§9Welcome to §6{store}§9!"),
        WITHDRAW_SUCCESS("§aYou have successfully withdrawn §6{amount}§a from the store!"),
        WORLD_NOT_FOUND("§cCould not find world ({world})!");

        public final String value;

        CipherKey(String value) {
            this.value = value;
        }
    }

    /**
     * The MultilineBuilder class assists in creating multi-line messages this is helpful for transaction messages if there are multiple messages to send
     */
    public static class AbstractMultilineBuilder {

        protected final StringBuilder finalMessage;
        protected final Map<String, Object> attributes;

        /**
         * Constructs a MultilineBuilder without any initial message
         */
        public AbstractMultilineBuilder() {
            finalMessage = new StringBuilder();
            attributes = null;
        }

        /**
         * Constructs a MultilineBuilder without any initial message, but adds attributes
         *
         * @param attributes the attributes to be parsed in the message
         */
        public AbstractMultilineBuilder(Map<String, Object> attributes) {
            finalMessage = new StringBuilder();
            this.attributes = attributes;
        }

        /**
         * Constructs a MultilineBuilder with an initial message
         *
         * @param message the initial message for the builder
         */
        public AbstractMultilineBuilder(String message) {
            finalMessage = new StringBuilder(message);
            attributes = null;
        }

        /**
         * Constructs a MultilineBuilder with an initial message and attributes
         *
         * @param message the initial message for the builder
         * @param attributes the attributes to be parsed in the message
         */
        public AbstractMultilineBuilder(String message, Map<String, Object> attributes) {
            finalMessage = new StringBuilder(format(message, attributes));
            this.attributes = attributes;
        }

        /**
         * Appends a message from the key
         *
         * @param key the key for determining the message value
         * @return the current instance
         */
        public AbstractMultilineBuilder append(CipherKey key) {
            return append(compose(key, attributes));
        }

        /**
         * Appends a message to a new line of the builder
         *
         * @param message the message to be appended
         * @return the current instance
         */
        public AbstractMultilineBuilder append(String message) {
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
        public AbstractMultilineBuilder appendf(CipherKey key, @Nullable Object... args) {
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
        public AbstractMultilineBuilder appendf(@NotNull String message, @Nullable Object... args) {
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
    }

    /**
     * Helper class for creating pages
     */
    public static class Paginator {
        private static final int pageSize = 10;

        static int calcPriceColWidth(Set<String> values) {
            int priceColWidth = 9;

            for (String value : values) {
                int priceWidth = value.length();
                if (priceWidth > priceColWidth) {
                    priceColWidth = priceWidth;
                }
            }

            return priceColWidth;
        }
    }
}
