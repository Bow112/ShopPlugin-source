package com.example.shopplugin.managers;

import com.example.shopplugin.ShopPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopManager {

    private final ShopPlugin plugin;
    private final Map<Material, Double> sellPrices = new HashMap<>();
    private final List<ShopItem> shopItems = new ArrayList<>();

    public ShopManager(ShopPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        sellPrices.clear();
        shopItems.clear();

        ConfigurationSection sellSection = plugin.getConfig().getConfigurationSection("sell-prices");
        if (sellSection != null) {
            for (String key : sellSection.getKeys(false)) {
                Material mat = Material.matchMaterial(key);
                if (mat != null) {
                    sellPrices.put(mat, sellSection.getDouble(key));
                } else {
                    plugin.getLogger().warning("Unknown material in sell-prices: " + key);
                }
            }
        }

        List<?> rawList = plugin.getConfig().getList("shop-items");
        if (rawList != null) {
            for (Object obj : rawList) {
                if (obj instanceof Map<?, ?> rawMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) rawMap;
                    String matStr = (String) map.get("material");
                    String name = map.containsKey("name") ? (String) map.get("name") : matStr;
                    Object priceObj = map.get("buy-price");
                    double buyPrice = priceObj instanceof Number ? ((Number) priceObj).doubleValue() : 0.0;
                    Material mat = Material.matchMaterial(matStr != null ? matStr : "");
                    if (mat != null) {
                        shopItems.add(new ShopItem(mat, name, buyPrice));
                    } else {
                        plugin.getLogger().warning("Unknown material in shop-items: " + matStr);
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded " + sellPrices.size() + " sell prices and " + shopItems.size() + " shop items.");
    }

    public Double getSellPrice(Material material) {
        return sellPrices.get(material);
    }

    public void setSellPrice(Material material, double price) {
        sellPrices.put(material, price);
        plugin.getConfig().set("sell-prices." + material.name(), price);
        plugin.saveConfig();
    }

    public List<ShopItem> getShopItems() {
        return shopItems;
    }

    public void addShopItem(Material material, String name, double buyPrice) {
        shopItems.removeIf(item -> item.getMaterial() == material);
        shopItems.add(new ShopItem(material, name, buyPrice));
        List<Map<String, Object>> serialized = new ArrayList<>();
        for (ShopItem item : shopItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("material", item.getMaterial().name());
            map.put("name", item.getDisplayName());
            map.put("buy-price", item.getBuyPrice());
            serialized.add(map);
        }
        plugin.getConfig().set("shop-items", serialized);
        plugin.saveConfig();
    }

    public boolean removeShopItem(Material material) {
        boolean removed = shopItems.removeIf(item -> item.getMaterial() == material);
        if (removed) {
            List<Map<String, Object>> serialized = new ArrayList<>();
            for (ShopItem item : shopItems) {
                Map<String, Object> map = new HashMap<>();
                map.put("material", item.getMaterial().name());
                map.put("name", item.getDisplayName());
                map.put("buy-price", item.getBuyPrice());
                serialized.add(map);
            }
            plugin.getConfig().set("shop-items", serialized);
            plugin.saveConfig();
        }
        return removed;
    }

    public static class ShopItem {
        private final Material material;
        private final String displayName;
        private final double buyPrice;

        public ShopItem(Material material, String displayName, double buyPrice) {
            this.material = material;
            this.displayName = displayName;
            this.buyPrice = buyPrice;
        }

        public Material getMaterial() { return material; }
        public String getDisplayName() { return displayName; }
        public double getBuyPrice() { return buyPrice; }
    }
}
