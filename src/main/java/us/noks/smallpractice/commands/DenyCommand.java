package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.objects.managers.RequestManager;

public class DenyCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /deny <player>");
			return false;
		}
		Player dueler = Bukkit.getPlayer(args[0]);
			
		if (dueler == null) {
			sender.sendMessage(ChatColor.RED + "This player is not online.");
			return false;
		}
		Player player = (Player) sender;
		
		if (dueler == player) {
			player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
			return false;
		}
		if (!PlayerManager.get(dueler.getUniqueId()).hasRequest(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "No request found!");
			return false;
		}
		RequestManager.getInstance().denyDuelRequest(player, dueler);
		return true;
	}
}