package us.noks.smallpractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.utils.PlayerStatus;

public class RandomCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only player can execute this command!");
			return false;
		}
		Player p = (Player) sender;
		
		if (PlayerManager.get(p).getStatus() != PlayerStatus.SPAWN) {
			p.sendMessage(ChatColor.RED + "You are not in the spawn!");
			return false;
		}
		
		Main.getInstance().addQueue(p);
		return false;
	}
}
