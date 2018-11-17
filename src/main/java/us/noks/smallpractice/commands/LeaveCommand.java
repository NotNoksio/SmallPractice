package us.noks.smallpractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.PlayerManager;
import us.noks.smallpractice.utils.PlayerStatus;

public class LeaveCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		PlayerManager pm = PlayerManager.get(player);
		
		if (args.length != 0) {
			player.sendMessage(ChatColor.RED + "Usage: /leave");
			return false;
		}
		if (pm.getStatus() != PlayerStatus.SPECTATE) {
			player.sendMessage(ChatColor.RED + "You are not in spectator mode.");
			return false;
		}
		player.setAllowFlight(false);
		player.setFlying(false);
		pm.setStatus(PlayerStatus.SPAWN);
		pm.showAllPlayer();
		player.getInventory().clear();
		player.teleport(Main.getInstance().spawnLocation);
		return false;
	}
}
