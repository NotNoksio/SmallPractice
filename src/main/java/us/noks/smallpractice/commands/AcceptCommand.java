package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.objects.managers.RequestManager;
import us.noks.smallpractice.utils.CustomMessages;

public class AcceptCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /accept <player>");
			return false;
		}
		Player dueler = Bukkit.getPlayer(args[0]);
			
		if (dueler == null) {
			sender.sendMessage(CustomMessages.getInstance().PLAYER_NOT_ONLINE);
			return false;
		}
		Player player = (Player) sender;
		
		if (dueler == player) {
			player.sendMessage(CustomMessages.getInstance().NOT_YOURSELF);
			return false;
		}
		if (PlayerManager.get(dueler.getUniqueId()).getRequestTo() != player.getUniqueId()) {
			player.sendMessage(CustomMessages.getInstance().NO_REQUEST_FOUND);
			return false;
		}
		RequestManager.getInstance().acceptDuelRequest(player, dueler);
		return true;
	}
}
