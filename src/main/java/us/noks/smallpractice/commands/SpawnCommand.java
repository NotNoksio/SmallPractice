package us.noks.smallpractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.PlayerManager;
import us.noks.smallpractice.utils.PlayerStatus;

public class SpawnCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		
		if (!player.isOp()) {
			player.sendMessage(ChatColor.RED + "No permission.");
			return false;
		}
		if (args.length > 0) {
			player.sendMessage(ChatColor.RED + "Usage: /spawn");
			return false;
		}
		if (PlayerManager.get(player).getStatus() != PlayerStatus.SPAWN) {
			player.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return false;
		}
		player.teleport(Main.getInstance().spawnLocation);
		player.sendMessage(ChatColor.GREEN + "Teleportation..");
		return false;
	}
}
