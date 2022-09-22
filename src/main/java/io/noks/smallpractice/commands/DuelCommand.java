package io.noks.smallpractice.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.objects.Cooldown;
import io.noks.smallpractice.objects.managers.PlayerManager;
import io.noks.smallpractice.party.Party;
import io.noks.smallpractice.party.PartyState;

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
		Party party = Main.getInstance().getPartyManager().getParty(player.getUniqueId());
		Party targetParty = Main.getInstance().getPartyManager().getParty(target.getUniqueId());
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
		boolean partyFight = party != null && targetParty != null;
		if (partyFight) {
			if (party == targetParty) {
				player.sendMessage(ChatColor.RED + "This player is in your own party.");
				return false;
			}
			if (!targetParty.getLeader().equals(target.getUniqueId())) {
				target = Bukkit.getPlayer(targetParty.getLeader());
			}
		}
		Cooldown cooldown = PlayerManager.get(player.getUniqueId()).getCooldown();
		if (cooldown.hasCooldown("DuelCommand")) {
			long secondsLeft = ((cooldown.getCooldownTime("DuelCommand") / 1000) + 5) - (System.currentTimeMillis() / 1000);
			if (secondsLeft > 0) {
				player.sendMessage(ChatColor.RED + "You cant sent duel request for another " + secondsLeft + " seconds!");
				return false;
			}
		}
		Main.getInstance().getRequestManager().openLadderSelectionIventory(player, target, partyFight);
		cooldown.addCooldown("DuelCommand", System.currentTimeMillis());
		return true;
	}
}
