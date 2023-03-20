package io.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.objects.managers.EloManager;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class StatsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length > 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /stats <player>");
			return false;
		}
		final Player player = (Player) sender;
		if (args.length == 0) {
			player.sendMessage(ChatColor.YELLOW + sender.getName() + ChatColor.DARK_AQUA + "'s stats:");
			final EloManager pe = PlayerManager.get(player.getUniqueId()).getEloManager();
			for (Ladders ladders : Ladders.values()) {
				player.sendMessage(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + ladders.getName() + ": " + ChatColor.YELLOW + pe.getFrom(ladders));
			}
			player.sendMessage(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Global: " + ChatColor.YELLOW + pe.getGlobal());
			return true;
		}
		final Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			player.sendMessage(ChatColor.RED + "Player not found.");
			return false;
		}
		player.sendMessage(ChatColor.YELLOW + target.getName() + ChatColor.DARK_AQUA + "'s stats:");
		final EloManager pe = PlayerManager.get(target.getUniqueId()).getEloManager();
		for (Ladders ladders : Ladders.values()) {
			player.sendMessage(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + ladders.getName() + ": " + ChatColor.YELLOW + pe.getFrom(ladders));
		}
		player.sendMessage(ChatColor.GRAY + "-> " + ChatColor.DARK_AQUA + "Global: " + ChatColor.YELLOW + pe.getGlobal());
		return true;
	}
}
