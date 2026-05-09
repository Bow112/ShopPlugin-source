package com.example.shopplugin.listeners;

import com.example.shopplugin.ShopPlugin;
import com.example.shopplugin.commands.ShopCommand;
import com.example.shopplugin.managers.EconomyManager;
import com.example.shopplugin.managers.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ShopListener implements Listener {

    private final ShopPlugin plugin;

    public ShopListener(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if this is our shop GUI
        String title = event.getView().getTitle();
        if (!title.equals(ShopCommand.SHOP_TITLE)) return;

        event.setCancelled(true); // Prevent taking items out

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Find the matching shop item by material
        List<ShopManager.ShopItem> shopItems = plugin.getShopManager().getShopItems();
        ShopManager.ShopItem found = null;
        for (ShopManager.ShopItem item : shopItems) {
            if (item.getMaterial() == clicked.getType()) {
                found = item;
                break;
            }
        }

        if (found == null) return;

        // Determine buy amount based on click type
        int buyAmount;
        ClickType clickType = event.getClick();
        if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            buyAmount = 16;
        } else if (clickType == ClickType.RIGHT) {
            buyAmount = 64;
        } else {
            buyAmount = 1;
        }

        EconomyManager economy = plugin.getEconomyManager();
        double totalCost = found.getBuyPrice() * buyAmount;

        if (!economy.hasEnough(player, totalCost)) {
            player.sendMessage("§c§lNot enough coins! §r§cYou need §e"
                    + economy.format(totalCost) + "§c but only have §e"
                    + economy.format(economy.getBalance(player)) + "§c.");
            return;
        }

        // Check inventory space
        ItemStack toGive = new ItemStack(found.getMaterial(), buyAmount);
        if (!hasInventorySpace(player, toGive)) {
            player.sendMessage("§cYou don't have enough inventory space!");
            return;
        }

        // Charge and give item
        economy.withdraw(player, totalCost);
        player.getInventory().addItem(toGive);

        String itemName = ChatColor.stripColor(clicked.getItemMeta() != null
                ? clicked.getItemMeta().getDisplayName()
                : found.getMaterial().name());

        player.sendMessage("§a§lPurchased! §r§aBought §e" + buyAmount + "x " + itemName
                + "§a for §e" + economy.format(totalCost) + "§a.");
        player.sendMessage("§aRemaining balance: §e" + economy.format(economy.getBalance(player)));
    }

    private boolean hasInventorySpace(Player player, ItemStack item) {
        // Check if addItem would fail (non-empty map means something didn't fit)
        var result = player.getInventory().addItem(item.clone());
        if (!result.isEmpty()) {
            // No space — we just tested with a clone so inventory is unchanged
            return false;
        }
        // We accidentally added it — remove it back
        player.getInventory().removeItem(item.clone());
        return true;
    }
}
