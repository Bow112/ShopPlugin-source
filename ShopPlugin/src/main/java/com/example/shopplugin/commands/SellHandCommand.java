package com.example.shopplugin.commands;

import com.example.shopplugin.ShopPlugin;
import com.example.shopplugin.managers.EconomyManager;
import com.example.shopplugin.managers.ShopManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SellHandCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public SellHandCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("shopplugin.sell")) {
            player.sendMessage("§cYou don't have permission to use /sellhand.");
            return true;
        }

        ItemStack held = player.getInventory().getItemInMainHand();

        if (held == null || held.getType() == Material.AIR) {
            player.sendMessage("§cYou're not holding anything!");
            return true;
        }

        ShopManager shopManager = plugin.getShopManager();
        EconomyManager economy = plugin.getEconomyManager();

        Double pricePerItem = shopManager.getSellPrice(held.getType());

        if (pricePerItem == null || pricePerItem <= 0) {
            player.sendMessage("§cThis item cannot be sold! (" + held.getType().name() + ")");
            return true;
        }

        // Determine how many to sell
        int amount = held.getAmount();

        // Optional: /sellhand <amount>
        if (args.length > 0) {
            try {
                int requested = Integer.parseInt(args[0]);
                if (requested <= 0) {
                    player.sendMessage("§cAmount must be greater than 0.");
                    return true;
                }
                if (requested > amount) {
                    player.sendMessage("§cYou only have " + amount + " of that item.");
                    return true;
                }
                amount = requested;
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid amount. Usage: /sellhand [amount]");
                return true;
            }
        }

        double totalEarned = pricePerItem * amount;

        // Remove items from inventory
        ItemStack toRemove = new ItemStack(held.getType(), amount);
        player.getInventory().removeItem(toRemove);

        // Give money
        economy.deposit(player, totalEarned);

        String itemName = formatMaterialName(held.getType());
        player.sendMessage("§a§lSold! §r§aSold §e" + amount + "x " + itemName
                + "§a for §e" + economy.format(totalEarned)
                + " §a(§e" + economy.format(pricePerItem) + "§a each)");
        player.sendMessage("§aNew balance: §e" + economy.format(economy.getBalance(player)));

        return true;
    }

    private String formatMaterialName(Material material) {
        // Convert OAK_LOG -> Oak Log
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
