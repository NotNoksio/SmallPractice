package io.noks.smallpractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.managers.PlayerManager;

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
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
		if (pm.getStatus() != PlayerStatus.SPAWN && pm.getStatus() != PlayerStatus.MODERATION && !pm.isAllowedToBuild()) {
			player.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return false;
		}
		player.teleport(player.getWorld().getSpawnLocation());
		player.sendMessage(ChatColor.GREEN + "Teleportation..");
		if (pm.getStatus() == PlayerStatus.BRIDGE) {
			pm.setStatus(PlayerStatus.SPAWN);	
			Main.getInstance().getItemManager().giveSpawnItem(player);
		}
		return true;
	}
}
