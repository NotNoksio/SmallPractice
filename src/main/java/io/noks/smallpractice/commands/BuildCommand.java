package io.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class BuildCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length > 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /build <player>");
			return false;
		}
		// Add /build <player>
		if (!sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "No permission.");
			return false;
		}
		Player player = (Player) sender;
		if (args.length == 0) {
			PlayerManager pm = PlayerManager.get(player.getUniqueId());
				
			pm.setStatus((pm.isAllowedToBuild() ? PlayerStatus.SPAWN : PlayerStatus.BUILD));
			player.sendMessage(ChatColor.DARK_AQUA + "Build state updated: " + (pm.isAllowedToBuild() ? ChatColor.GREEN + "Activated" : ChatColor.RED + "Deactivated"));
			return true;
		}
		Player target = Bukkit.getPlayer(args[0]);
		
		if (target == null) {
			player.sendMessage(ChatColor.RED + "This player is not online.");
			return false;
		}
		if (target == player) {
			player.sendMessage(ChatColor.RED + "Execute: /build");
			return false;
		}
		PlayerManager tm = PlayerManager.get(target.getUniqueId());
		
		tm.setStatus((tm.isAllowedToBuild() ? PlayerStatus.SPAWN : PlayerStatus.BUILD));
		target.sendMessage(ChatColor.DARK_AQUA + "Build state updated by " + player.getName() + ": " + (tm.isAllowedToBuild() ? ChatColor.GREEN + "Activated" : ChatColor.RED + "Deactivated"));
		player.sendMessage(ChatColor.DARK_AQUA + "Build state updated for " + target.getName() + ": " + (tm.isAllowedToBuild() ? ChatColor.GREEN + "Activated" : ChatColor.RED + "Deactivated"));
		return true;
	}
}
