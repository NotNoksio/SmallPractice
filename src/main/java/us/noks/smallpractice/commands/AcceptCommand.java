package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.Request;
import us.noks.smallpractice.objects.managers.PlayerManager;

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
			sender.sendMessage(ChatColor.RED + "This player is not online.");
			return false;
		}
		Player player = (Player) sender;
		
		if (dueler == player) {
			player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
			return false;
		}
		PlayerManager dm = PlayerManager.get(dueler.getUniqueId());
		if (!dm.hasRequested(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "No request found!");
			return false;
		}
		Request request = dm.getRequests().get(player.getUniqueId());
		Main.getInstance().getRequestManager().acceptDuelRequest(request.getArena(), request.getLadder(), player, dueler);
		return true;
	}
}
