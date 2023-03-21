package io.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class FreezeCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (!sender.hasPermission("command.freeze")) {
			sender.sendMessage(ChatColor.RED + "No permission.");
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /freeze <player>");
			return false;
		}
		final Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "INVALID Player!");
			return false;
		}
		if (target == (Player) sender) {
			sender.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
			return false;
		}
		final PlayerManager tm = PlayerManager.get(target.getUniqueId());
		if (tm.isFrozen()) {
			tm.setStatus(tm.getPreviousStatus());
			target.sendMessage(ChatColor.RED + "You have been unfrozen by " + sender.getName());
			sender.sendMessage(ChatColor.GREEN + "You have unfrozen " + target.getName());
			return true;
		}
		tm.setStatus(PlayerStatus.FREEZE);
		target.sendMessage(ChatColor.RED + "You have been freezed by " + sender.getName());
		sender.sendMessage(ChatColor.GREEN + "You have freezed " + target.getName());
		return true;
	}
}
