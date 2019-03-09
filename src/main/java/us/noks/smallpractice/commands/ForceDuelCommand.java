package us.noks.smallpractice.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.DuelManager;
import us.noks.smallpractice.objects.managers.PartyManager;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.utils.Messages;

public class ForceDuelCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		PlayerManager pm = PlayerManager.get(player);
		
		if (!player.hasPermission("command.forceduel")) {
			player.sendMessage(Messages.NO_PERMISSION);
			return false;
		}
		if (args.length != 1) {
			player.sendMessage(ChatColor.RED + "Usage: /forceduel <player>");
			return false;
		}
		if (pm.getStatus() != PlayerStatus.SPAWN) {
			player.sendMessage(ChatColor.RED + "You are not in the spawn.");
			return false;
		}
		Player target = Bukkit.getPlayer(args[0]);
		
		if (target == null) {
			player.sendMessage(Messages.PLAYER_NOT_ONLINE);
			return false;
		}
		if (target == player) {
			player.sendMessage(Messages.NOT_YOURSELF);
			return false;
		}
		if (PartyManager.getInstance().hasParty(target.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "That player is in a party!");
			return false;
		}
		PlayerManager tm = PlayerManager.get(target);
		
		if (tm.getStatus() != PlayerStatus.SPAWN) {
			player.sendMessage(ChatColor.RED + "This player is not in the spawn.");
			return false;
		}
		List<UUID> firstTeam = Lists.newArrayList();
		firstTeam.add(player.getUniqueId());
		List<UUID> secondTeam = Lists.newArrayList();
		secondTeam.add(target.getUniqueId());
		
		DuelManager.getInstance().startDuel(null, null, firstTeam, secondTeam, false, 1);
		return false;
	}
}
