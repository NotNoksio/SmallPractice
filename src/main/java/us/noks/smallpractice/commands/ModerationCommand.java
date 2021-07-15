package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class ModerationCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (!sender.hasPermission("command.moderation")) {
			sender.sendMessage(ChatColor.RED + "No permission.");
			return false;
		}
		if (args.length > 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /mod");
			return false;
		}
		Player player = (Player) sender;
			
		if (Main.getInstance().getPartyManager().hasParty(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "You are in party!");
			return false;
		}
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
			
		if (pm.getStatus() == PlayerStatus.MODERATION) {
			player.teleport(player.getWorld().getSpawnLocation());
			pm.setStatus(PlayerStatus.SPAWN);
			Main.getInstance().getItemManager().giveSpawnItem(player);
			pm.showAllPlayer();
			return true;
		}
		if (pm.getStatus() != PlayerStatus.SPAWN) {
			player.sendMessage(ChatColor.RED + "You cant execute this command on your current state!");
			return false;
		}
			
		pm.setStatus(PlayerStatus.MODERATION);
		for (Player allPlayers : Bukkit.getOnlinePlayers()) {
			allPlayers.hidePlayer(player);
			player.showPlayer(allPlayers);
		}
		Main.getInstance().getItemManager().giveModerationItem(player);
		return true;
	}
}
