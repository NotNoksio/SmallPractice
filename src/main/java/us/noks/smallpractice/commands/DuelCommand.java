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
			Player player = (Player) sender;
			
			if (args.length != 1) {
				player.sendMessage(ChatColor.RED + "Usage: /duel <player>");
				return false;
			}
			Player target = Bukkit.getPlayer(args[0]);
			
			if (target == null) {
				player.sendMessage(ChatColor.RED + "That player is not online!");
				return false;
			}
			if (target == player) {
				player.sendMessage(ChatColor.RED + "You can't duel yourself!");
				return false;
			}
			if (cooldowns.containsKey(player.getUniqueId())) {
				long secondsLeft = ((cooldowns.get(player.getUniqueId()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
	            if (secondsLeft > 0) {
	                player.sendMessage(ChatColor.RED + "You cant sent duel request for another " + secondsLeft + " seconds!");
	                return false;
	            }
			}
			Main.getInstance().sendDuelRequest(player, target);
			cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
		}
		return false;
	}
}
