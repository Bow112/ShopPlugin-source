package com.example.shopplugin.commands;

import com.example.shopplugin.ShopPlugin;
import com.example.shopplugin.managers.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ShopCommand implements CommandExecutor {

    private final ShopPlugin plugin;
    // Title used to identify the shop GUI (also shown to player)
    public static final String SHOP_TITLE = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "★ Player Shop ★";

    public ShopCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("shopplugin.shop")) {
            player.sendMessage("§cYou don't have permission to use /shop.");
            return true;
        }

        openShop(player);
        return true;
    }

    public void openShop(Player player) {
        List<ShopManager.ShopItem> shopItems = plugin.getShopManager().getShopItems();

        // Calculate rows needed (max 54 slots = 6 rows)
        int size = Math.min(54, (int) Math.ceil((shopItems.size() + 9) / 9.0) * 9);
        if (size < 9) size = 9;

        Inventory gui = Bukkit.createInventory(null, size, SHOP_TITLE);

        // Fill shop items
        for (int i = 0; i < shopItems.size() && i < size; i++) {
            ShopManager.ShopItem shopItem = shopItems.get(i);
            ItemStack displayItem = new ItemStack(shopItem.getMaterial());
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + shopItem.getDisplayName());

                Double sellPrice = plugin.getShopManager().getSellPrice(shopItem.getMaterial());
                String sellLine = sellPrice != null
                        ? ChatColor.GREEN + "  Sell: " + ChatColor.WHITE + plugin.getEconomyManager().format(sellPrice) + " each"
                        : ChatColor.RED + "  Not sellable";

                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "────────────────",
                        ChatColor.GREEN + "  Buy:  " + ChatColor.WHITE + plugin.getEconomyManager().format(shopItem.getBuyPrice()) + " each",
                        sellLine,
                        ChatColor.GRAY + "────────────────",
                        ChatColor.AQUA + "Left-click  " + ChatColor.WHITE + "→ Buy 1",
                        ChatColor.AQUA + "Right-click " + ChatColor.WHITE + "→ Buy 64",
                        ChatColor.AQUA + "Shift+click " + ChatColor.WHITE + "→ Buy 16"
                ));
                displayItem.setItemMeta(meta);
            }
            gui.setItem(i, displayItem);
        }

        // Fill remaining bottom row with glass pane decoration
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = shopItems.size(); i < size; i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }

        player.openInventory(gui);
        player.sendMessage("§a§lShop opened! §r§7Left-click to buy 1, right-click for 64, shift+click for 16.");
    }
}
