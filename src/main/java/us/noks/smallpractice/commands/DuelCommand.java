package us.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.objects.CommandCooldown;
import us.noks.smallpractice.objects.managers.PartyManager;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.objects.managers.RequestManager;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;

public class DuelCommand implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length != 1) {
			sender.sendMessage(ChatColor.RED + "Usage: /duel <player>");
			return false;
		}
		Player target = Bukkit.getPlayer(args[0]);
			
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "This player is not online.");
			return false;
		}
		Player player = (Player) sender;
			
		if (target == player) {
			player.sendMessage(ChatColor.RED + "You can't execute that command on yourself!");
			return false;
		}
		Party party = PartyManager.getInstance().getParty(player.getUniqueId());
		Party targetParty = PartyManager.getInstance().getParty(target.getUniqueId());
		if (party != null) {
			if (!party.getLeader().equals(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "You are not the leader of this party!");
				return false;
			}
			if (targetParty == null) {
				player.sendMessage(ChatColor.RED + "This player is not in a party!");
				return false;
			}
			if (targetParty.getPartyState() == PartyState.DUELING) {
				player.sendMessage(ChatColor.RED + "This party is currently busy.");
				return false;
			}
		} else if (targetParty != null) {
			player.sendMessage(ChatColor.RED + "This player is in a party!");
			return false;
		}
		if (party != null && targetParty != null) {
			if (party == targetParty) {
				player.sendMessage(ChatColor.RED + "This player is in your own party.");
				return false;
			}
			if (!targetParty.getLeader().equals(target.getUniqueId())) {
				target = Bukkit.getPlayer(targetParty.getLeader());
			}
		}
		CommandCooldown cooldown = PlayerManager.get(player.getUniqueId()).getCooldown();
		if (cooldown.hasCooldown("Duel")) {
			long secondsLeft = ((cooldown.getCooldownTime("Duel") / 1000) + 5) - (System.currentTimeMillis() / 1000);
			if (secondsLeft > 0) {
				player.sendMessage(ChatColor.RED + "You cant sent duel request for another " + secondsLeft + " seconds!");
				return false;
			}
		}
		RequestManager.getInstance().openLadderSelectionIventory(player, target);
		cooldown.addCooldown("Duel", System.currentTimeMillis());
		return true;
	}
}
