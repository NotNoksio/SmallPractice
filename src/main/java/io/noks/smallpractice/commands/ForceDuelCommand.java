package io.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.arena.Arena;
import io.noks.smallpractice.enums.Ladders;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.managers.PlayerManager;

public class ForceDuelCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (!sender.hasPermission("command.forceduel")) {
			sender.sendMessage(ChatColor.RED + "No permission.");
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /forceduel <player>");
			return false;
		}
		final Player player = (Player) sender;
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		
		if (pm.getStatus() != PlayerStatus.SPAWN) {
			player.sendMessage(ChatColor.RED + "You are not in the spawn.");
			return false;
		}
		if (Main.getInstance().getPartyManager().hasParty(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "You are in a party!");
			return false;
		}
		final Player target = Bukkit.getPlayer(args[0]);
		
		if (target == null) {
			player.sendMessage(ChatColor.RED + "This player is not online.");
			return false;
		}
		if (target == player) {
			player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
			return false;
		}
		if (Main.getInstance().getPartyManager().hasParty(target.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "That player is in a party!");
			return false;
		}
		final PlayerManager tm = PlayerManager.get(target.getUniqueId());
		
		if (!tm.isAlive()) {
			player.sendMessage(ChatColor.RED + "This player is not in the spawn.");
			return false;
		}
		if (tm.getStatus() != PlayerStatus.QUEUE) {
			player.sendMessage(ChatColor.RED + "This player must be in queue!");
			return false;
		}
		player.sendMessage(ChatColor.RED + "Code under review due to a bug :)");
		//final Ladders ladder = Main.getInstance().getQueueManager().getQueueMap().get(target.getUniqueId()).getLadder();
		//Main.getInstance().getDuelManager().startDuel(Arena.getInstance().getRandomArena(ladder), ladder, player.getUniqueId(), target.getUniqueId(), false);
		// TODO: remove target from queue
		return true;
	}
}
