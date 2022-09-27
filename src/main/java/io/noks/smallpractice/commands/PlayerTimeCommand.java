package io.noks.smallpractice.commands;

import org.bukkit.Bukkit;
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
			sender.sendMessage(ChatColor.RED + "Usage: /sunrise:day:sunset:night");
			return false;
		}
		Bukkit.broadcastMessage(cmd.getName());
		final PlayerTimeEnum pt = PlayerTimeEnum.getEnumByName(cmd.getName().toLowerCase());
		if (pt == null) {
			sender.sendMessage(ChatColor.RED + "Usage: /sunrise:day:sunset:night");
			return false;
		}
		final Player player = (Player) sender;
		player.setPlayerTime(pt.getTime(), false);
        player.sendMessage(ChatColor.GREEN + "You have set the " + pt.getName().toLowerCase() + ".");
		return true;
	}
}