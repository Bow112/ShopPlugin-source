package com.example.shopplugin.commands;

import com.example.shopplugin.ShopPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public BalanceCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("shopplugin.balance")) {
            player.sendMessage("§cYou don't have permission to check your balance.");
            return true;
        }

        double balance = plugin.getEconomyManager().getBalance(player);
        player.sendMessage("§6§l» §e" + plugin.getCurrencyName() + " Balance: §a"
                + plugin.getEconomyManager().format(balance));
        return true;
    }
}
