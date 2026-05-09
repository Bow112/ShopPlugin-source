package com.example.shopplugin.managers;

import com.example.shopplugin.ShopPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final ShopPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    // Cache: UUID -> balance
    private final Map<UUID, Double> balances = new HashMap<>();

    public EconomyManager(ShopPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "balances.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create balances.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load all saved balances into memory
        if (dataConfig.contains("balances")) {
            for (String key : dataConfig.getConfigurationSection("balances").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    double balance = dataConfig.getDouble("balances." + key);
                    balances.put(uuid, balance);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        plugin.getLogger().info("Loaded " + balances.size() + " player balances.");
    }

    public void saveData() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            dataConfig.set("balances." + entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save balances.yml: " + e.getMessage());
        }
    }

    public double getBalance(Player player) {
        return balances.computeIfAbsent(player.getUniqueId(), k -> {
            double startingBalance = plugin.getConfig().getDouble("starting-balance", 100.0);
            return startingBalance;
        });
    }

    public void setBalance(Player player, double amount) {
        balances.put(player.getUniqueId(), Math.max(0, amount));
        // Auto-save on change (you could batch this instead for performance)
        saveData();
    }

    public boolean withdraw(Player player, double amount) {
        double balance = getBalance(player);
        if (balance < amount) return false;
        setBalance(player, balance - amount);
        return true;
    }

    public void deposit(Player player, double amount) {
        setBalance(player, getBalance(player) + amount);
    }

    public boolean hasEnough(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    /** Format a double as a nice price string, e.g. $1,250.00 */
    public String format(double amount) {
        return plugin.getCurrencySymbol() + String.format("%,.2f", amount);
    }
}
