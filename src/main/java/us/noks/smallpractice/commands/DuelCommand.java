package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.Main;

public class DuelCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			
			if (args.length != 1) {
				p.sendMessage(ChatColor.RED + "Usage: /duel <player>");
				return false;
			}
			
			Player target = Bukkit.getPlayer(args[0]);
			
			if (target == null) {
				p.sendMessage(ChatColor.RED + "That player is not online!");
				return false;
			}
			if (target == p) {
				p.sendMessage(ChatColor.RED + "You can't duel yourself!");
				return false;
			}
			Main.getInstance().sendDuelRequest(p, target);
		}
		return false;
	}
}
