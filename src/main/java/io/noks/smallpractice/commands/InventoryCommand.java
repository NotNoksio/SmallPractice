package io.noks.smallpractice.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class InventoryCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /inventory <UUID>");
			return false;
		}
		if (!args[0].matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")) {
			sender.sendMessage(ChatColor.RED + "UUID not found!");
			return false;
		}
		final Player player = (Player) sender;
		final UUID targetUUID = UUID.fromString(args[0]);
		final PlayerManager tm = PlayerManager.get(targetUUID);
		
		if (tm.getSavedInventory() == null) {
			if (Main.getInstance().getOfflineInventories().containsKey(targetUUID)) {
				player.openInventory(Main.getInstance().getOfflineInventories().get(targetUUID));
				return true;
			}
			player.sendMessage(ChatColor.RED + "Inventory expired!");
			return false;
		}
		player.openInventory(tm.getSavedInventory());
		return true;
	}
}