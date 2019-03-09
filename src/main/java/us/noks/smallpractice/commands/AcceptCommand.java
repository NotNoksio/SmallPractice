package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.utils.Messages;

public class AcceptCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			
			if (args.length != 1) {
				p.sendMessage(ChatColor.RED + "Usage: /accept <player>");
				return false;
			}
			Player dueler = Bukkit.getPlayer(args[0]);
			
			if (dueler == null) {
				p.sendMessage(Messages.PLAYER_NOT_ONLINE);
				return false;
			}
			if (dueler == p) {
				p.sendMessage(ChatColor.RED + "You can't accept yourself!");
				return false;
			}
			Main.getInstance().acceptDuelRequest(p, dueler);
		}
		return true;
	}
}
