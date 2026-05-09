package com.example.shopplugin.commands;

import com.example.shopplugin.ShopPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AddShopItemCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public AddShopItemCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("shopplugin.admin")) {
            player.sendMessage("§cYou don't have permission to use /addshopitem.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /addshopitem <buyPrice>");
            player.sendMessage("§7Hold the item you want to add to the shop.");
            return true;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) {
            player.sendMessage("§cHold the item you want to add to the shop!");
            return true;
        }

        double buyPrice;
        try {
            buyPrice = Double.parseDouble(args[0]);
            if (buyPrice <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid price. Must be a positive number.");
            return true;
        }

        // Use custom display name if the item has one, otherwise format material name
        String displayName;
        ItemMeta meta = held.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            displayName = meta.getDisplayName();
        } else {
            displayName = formatMaterialName(held.getType());
        }

        plugin.getShopManager().addShopItem(held.getType(), displayName, buyPrice);

        player.sendMessage("§a§lAdded to shop! §r§e" + displayName
                + " §acan now be bought for §e" + plugin.getEconomyManager().format(buyPrice) + "§a.");
        player.sendMessage("§7Tip: Use §f/setprice §7to also set a sell price for this item.");
        return true;
    }

    private String formatMaterialName(Material material) {
        String[] words = material.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(Character.toUpperCase(word.charAt(0)));
            sb.append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
