package us.noks.smallpractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.utils.Messages;

public class BuildCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length != 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /build");
			return false;
		}
		Player player = (Player) sender;
			
		if (!player.hasPermission("command.build")) {
			player.sendMessage(Messages.getInstance().NO_PERMISSION);
			return false;
		}
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
			
		pm.setStatus((pm.isCanBuild() ? PlayerStatus.SPAWN : PlayerStatus.BUILD));
		player.sendMessage(ChatColor.DARK_AQUA + "Build state updated: " + ChatColor.YELLOW + pm.isCanBuild());
		return true;
	}
}
