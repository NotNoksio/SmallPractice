package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.utils.Messages;

public class PingCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			if(args.length > 1) {
				player.sendMessage(ChatColor.RED + "Usage: /ping <player>");
				return false;
			}
			
			if (args.length == 0) {
				player.sendMessage(ChatColor.DARK_AQUA + "Your ping: " + ChatColor.YELLOW + player.spigot().getPing() + "ms");
				return true;
			}
			
			if (args.length == 1) {
				Player target = Bukkit.getPlayer(args[0]);
				
				if (target == null) {
					player.sendMessage(Messages.PLAYER_NOT_ONLINE);
					return false;
				}
				if (target == player) {
					player.sendMessage(ChatColor.DARK_AQUA + "Your ping: " + ChatColor.YELLOW + player.spigot().getPing() + "ms");
					return true;
				}
				
				player.sendMessage(ChatColor.GREEN + target.getName() + ChatColor.DARK_AQUA + "'s ping: " + ChatColor.YELLOW + target.spigot().getPing() + "ms");
				player.sendMessage(ChatColor.DARK_AQUA + "Ping difference: " + ChatColor.YELLOW + Math.abs(player.spigot().getPing() - target.spigot().getPing()) + "ms");
			}
		}
		return false;
	}
}
