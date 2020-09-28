package us.noks.smallpractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.ItemManager;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class SpawnCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length > 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /spawn");
			return false;
		}
		Player player = (Player) sender;
		if (PlayerManager.get(player.getUniqueId()).getStatus() != PlayerStatus.SPAWN && PlayerManager.get(player.getUniqueId()).getStatus() != PlayerStatus.MODERATION && !PlayerManager.get(player.getUniqueId()).isCanBuild()) {
			player.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return false;
		}
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
		player.teleport(player.getWorld().getSpawnLocation());
		player.sendMessage(ChatColor.GREEN + "Teleportation..");
		if (pm.getStatus() == PlayerStatus.BRIDGE) {
			pm.setStatus(PlayerStatus.SPAWN);
			ItemManager.getInstace().giveSpawnItem(player);
		}
		return true;
	}
}
