package com.example.shopplugin;

import com.example.shopplugin.commands.*;
import com.example.shopplugin.listeners.ShopListener;
import com.example.shopplugin.managers.EconomyManager;
import com.example.shopplugin.managers.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private EconomyManager economyManager;
    private ShopManager shopManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Init managers
        economyManager = new EconomyManager(this);
        shopManager = new ShopManager(this);

        // Register commands
        getCommand("sellhand").setExecutor(new SellHandCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("setprice").setExecutor(new SetPriceCommand(this));
        getCommand("addshopitem").setExecutor(new AddShopItemCommand(this));
        getCommand("removeshopitem").setExecutor(new RemoveShopItemCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        getLogger().info("ShopPlugin enabled! Commands: /sellhand /shop /balance");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.saveData();
        }
        getLogger().info("ShopPlugin disabled. Data saved.");
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public String getCurrencySymbol() {
        return getConfig().getString("currency-symbol", "$");
    }

    public String getCurrencyName() {
        return getConfig().getString("currency-name", "Coins");
    }
}
