package us.noks.smallpractice.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.Main;

public class DuelCommand implements CommandExecutor {
	
	private int cooldownTime = 5;
	private Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

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
			if (cooldowns.containsKey(((Player) sender).getUniqueId())) {
				long secondsLeft = ((cooldowns.get(((Player) sender).getUniqueId()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
	            if (secondsLeft > 0) {
	                sender.sendMessage(org.bukkit.ChatColor.RED + "You cant sent duel request for another " + secondsLeft + " seconds!");
	                return false;
	            }
			}
			Main.getInstance().sendDuelRequest(p, target);
			cooldowns.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
		}
		return false;
	}
}
