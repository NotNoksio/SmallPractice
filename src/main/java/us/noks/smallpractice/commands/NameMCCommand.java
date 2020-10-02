package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NameMCCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length != 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /namemc <player>");
				return false;
			}
			Player target = Bukkit.getPlayer(args[0]);
			
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "This player is not online.");
				return false;
			}
			Player player = (Player) sender;
			
			if (target == player) {
				player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
				return false;
			}
			player.sendMessage(ChatColor.YELLOW + target.getName() + "'s " + ChatColor.DARK_AQUA + "NameMC: " + ChatColor.RESET + "https://namemc.com/profile/" + target.getName());
		}
		return false;
	}
}
