package us.noks.smallpractice.commands;

import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MentionCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (!sender.hasPermission("command.mention")) {
			sender.sendMessage(ChatColor.RED + "No Permission.");
			return false;
		}
		if (args.length < 3) {
			sender.sendMessage(ChatColor.RED + "Usage: /mention <player> <+ / -> <msg>");
			return false;
		}
		Player player = (Player) sender;
		Player target = Bukkit.getPlayer(args[0]);

		if (target == null) {
			player.sendMessage(ChatColor.RED + "That player is not online!");
			return false;
		}
		if (target == player) {
			player.sendMessage(ChatColor.RED + "You cannot mention yourself!");
			return false;
		}
		if (!args[1].equalsIgnoreCase("+") && !args[1].equalsIgnoreCase("-")) {
			player.sendMessage(ChatColor.RED + "Usage: /mention <player> <+ / -> <msg>");
			return false;
		}
		
		StringJoiner joiner = new StringJoiner(" ");
		for (int i = 2; i < args.length; i++) {
			joiner.add(args[i]);
		}

		if (args[1].equals("+") || args[1].equals("-")) {
			Bukkit.broadcastMessage("");
			player.chat(ChatColor.YELLOW + target.getName() + ChatColor.GRAY + " > " + (args[1].equals("+") ? ChatColor.GREEN : ChatColor.RED) + joiner);
			Bukkit.broadcastMessage("");
			return true;
		}
		player.sendMessage(ChatColor.RED + "Usage: /mention <player> <+ / -> <msg>");
		return false;
	}
}