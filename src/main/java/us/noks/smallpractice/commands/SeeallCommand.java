package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.utils.Messages;

public class SeeallCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		if (!player.hasPermission("command.seeall")) {
			player.sendMessage(Messages.getInstance().NO_PERMISSION);
			return false;
		}
		if (args.length != 0) {
			player.sendMessage(ChatColor.RED + "Usage: /seeall");
			return false;
		}
		Bukkit.getOnlinePlayers().forEach((allplayers) -> {
			player.showPlayer(allplayers);
		});
		player.sendMessage(ChatColor.GREEN + "You see everyone right now.");
		return false;
	}
}
