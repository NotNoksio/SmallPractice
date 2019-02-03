package us.noks.smallpractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class BuildCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			
			if (!p.hasPermission("command.build")) {
				p.sendMessage(ChatColor.RED + "No permission.");
				return false;
			}
			if (args.length != 0) {
				p.sendMessage(ChatColor.RED + "Usage: /build");
				return false;
			}
			
			PlayerManager.get(p).setStatus((PlayerManager.get(p).isCanBuild() ? PlayerStatus.SPAWN : PlayerStatus.BUILD));
			p.sendMessage(ChatColor.DARK_AQUA + "Build state updated: " + ChatColor.YELLOW + PlayerManager.get(p).isCanBuild());
		}
		return true;
	}
}
