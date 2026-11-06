package net.sparkzz.shops.mocks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory Vault economy for tests. Balances are kept per player UUID and start at 0.
 * World-specific overloads ignore the world, and banks are not supported.
 */
public class FakeEconomy implements Economy {

    private final Map<UUID, Double> balances = new HashMap<>();

    private static OfflinePlayer player(String name) {
        return Bukkit.getOfflinePlayer(name);
    }

    @Override public boolean isEnabled() { return true; }
    @Override public String getName() { return "FakeEconomy"; }
    @Override public boolean hasBankSupport() { return false; }
    @Override public int fractionalDigits() { return 2; }
    @Override public String format(double amount) { return String.format("%.2f", amount); }
    @Override public String currencyNamePlural() { return "dollars"; }
    @Override public String currencyNameSingular() { return "dollar"; }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return balances.containsKey(player.getUniqueId());
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return balances.getOrDefault(player.getUniqueId(), 0D);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0)
            return new EconomyResponse(0, getBalance(player), ResponseType.FAILURE, "Cannot withdraw a negative amount");

        if (!has(player, amount))
            return new EconomyResponse(0, getBalance(player), ResponseType.FAILURE, "Insufficient funds");

        double balance = getBalance(player) - amount;
        balances.put(player.getUniqueId(), balance);
        return new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0)
            return new EconomyResponse(0, getBalance(player), ResponseType.FAILURE, "Cannot deposit a negative amount");

        double balance = getBalance(player) + amount;
        balances.put(player.getUniqueId(), balance);
        return new EconomyResponse(amount, balance, ResponseType.SUCCESS, null);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return balances.putIfAbsent(player.getUniqueId(), 0D) == null;
    }

    // Name-based and world-based overloads delegate to the OfflinePlayer versions above

    @Override public boolean hasAccount(String name) { return hasAccount(player(name)); }
    @Override public boolean hasAccount(String name, String world) { return hasAccount(player(name)); }
    @Override public boolean hasAccount(OfflinePlayer player, String world) { return hasAccount(player); }
    @Override public double getBalance(String name) { return getBalance(player(name)); }
    @Override public double getBalance(String name, String world) { return getBalance(player(name)); }
    @Override public double getBalance(OfflinePlayer player, String world) { return getBalance(player); }
    @Override public boolean has(String name, double amount) { return has(player(name), amount); }
    @Override public boolean has(String name, String world, double amount) { return has(player(name), amount); }
    @Override public boolean has(OfflinePlayer player, String world, double amount) { return has(player, amount); }
    @Override public EconomyResponse withdrawPlayer(String name, double amount) { return withdrawPlayer(player(name), amount); }
    @Override public EconomyResponse withdrawPlayer(String name, String world, double amount) { return withdrawPlayer(player(name), amount); }
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer player, String world, double amount) { return withdrawPlayer(player, amount); }
    @Override public EconomyResponse depositPlayer(String name, double amount) { return depositPlayer(player(name), amount); }
    @Override public EconomyResponse depositPlayer(String name, String world, double amount) { return depositPlayer(player(name), amount); }
    @Override public EconomyResponse depositPlayer(OfflinePlayer player, String world, double amount) { return depositPlayer(player, amount); }
    @Override public boolean createPlayerAccount(String name) { return createPlayerAccount(player(name)); }
    @Override public boolean createPlayerAccount(String name, String world) { return createPlayerAccount(player(name)); }
    @Override public boolean createPlayerAccount(OfflinePlayer player, String world) { return createPlayerAccount(player); }

    // Banks are not supported

    private static EconomyResponse noBanks() {
        return new EconomyResponse(0, 0, ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }

    @Override public EconomyResponse createBank(String name, String player) { return noBanks(); }
    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return noBanks(); }
    @Override public EconomyResponse deleteBank(String name) { return noBanks(); }
    @Override public EconomyResponse bankBalance(String name) { return noBanks(); }
    @Override public EconomyResponse bankHas(String name, double amount) { return noBanks(); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return noBanks(); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return noBanks(); }
    @Override public EconomyResponse isBankOwner(String name, String player) { return noBanks(); }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return noBanks(); }
    @Override public EconomyResponse isBankMember(String name, String player) { return noBanks(); }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return noBanks(); }
    @Override public List<String> getBanks() { return List.of(); }
}
