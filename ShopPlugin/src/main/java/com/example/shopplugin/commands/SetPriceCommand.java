package com.example.shopplugin.commands;

import com.example.shopplugin.ShopPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetPriceCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public SetPriceCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("shopplugin.admin")) {
            player.sendMessage("§cYou don't have permission to use /setprice.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /setprice <price>");
            player.sendMessage("§7Hold the item you want to set the sell price for.");
            return true;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) {
            player.sendMessage("§cHold the item you want to set the price for!");
            return true;
        }

        double price;
        try {
            price = Double.parseDouble(args[0]);
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid price. Must be a positive number.");
            return true;
        }

        plugin.getShopManager().setSellPrice(held.getType(), price);

        String matName = held.getType().name();
        player.sendMessage("§a§lDone! §r§aSell price for §e" + matName
                + " §aset to §e" + plugin.getEconomyManager().format(price) + "§a per item.");
        return true;
    }
}
