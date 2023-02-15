package io.noks.smallpractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.enums.PlayerTimeEnum;

public class PlayerTimeCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length != 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /sunrise:day:sunset:night:resettime");
			return false;
		}
		final Player player = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("resettime")) {
			player.resetPlayerTime();
	        player.sendMessage(ChatColor.GREEN + "You have reset your sky time!");
			return true;
		}
		final PlayerTimeEnum pt = PlayerTimeEnum.getEnumByName(cmd.getName().toLowerCase());
		if (pt == null) {
			sender.sendMessage(ChatColor.RED + "Usage: /sunrise:day:sunset:night");
			return false;
		}
		player.setPlayerTime(pt.getTime(), false);
        player.sendMessage(ChatColor.GREEN + "You've set the " + pt.getName().toLowerCase() + ".");
        player.sendMessage(ChatColor.GRAY + "type \"/resettime\" to reset the sky time.");
		return true;
	}
}