package com.example.shopplugin.commands;

import com.example.shopplugin.ShopPlugin;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveShopItemCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public RemoveShopItemCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.hasPermission("shopplugin.admin")) {
            player.sendMessage("§cYou don't have permission to use /removeshopitem.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage: /removeshopitem <MATERIAL_NAME>");
            player.sendMessage("§7Example: /removeshopitem DIAMOND");
            return true;
        }

        Material mat = Material.matchMaterial(args[0]);
        if (mat == null) {
            player.sendMessage("§cUnknown material: " + args[0]);
            player.sendMessage("§7Use the exact Minecraft material name, e.g. OAK_LOG, IRON_INGOT");
            return true;
        }

        boolean removed = plugin.getShopManager().removeShopItem(mat);
        if (removed) {
            player.sendMessage("§a§lRemoved! §r§e" + mat.name() + " §ahas been removed from the shop.");
        } else {
            player.sendMessage("§c" + mat.name() + " was not found in the shop.");
        }
        return true;
    }
}
