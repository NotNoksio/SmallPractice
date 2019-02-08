package us.noks.smallpractice.commands;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.minecraft.util.com.google.common.collect.Maps;
import us.noks.smallpractice.Main;
import us.noks.smallpractice.objects.managers.PartyManager;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;

public class DuelCommand implements CommandExecutor {
	
	private int cooldownTime = 5;
	private Map<UUID, Long> cooldowns = Maps.newConcurrentMap();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			if (args.length != 1) {
				player.sendMessage(ChatColor.RED + "Usage: /duel <player>");
				return false;
			}
			Player target = Bukkit.getPlayer(args[0]);
			
			if (target == null) {
				player.sendMessage(ChatColor.RED + "That player is not online!");
				return false;
			}
			if (target == player) {
				player.sendMessage(ChatColor.RED + "You can't duel yourself!");
				return false;
			}
			Party party = PartyManager.getInstance().getParty(player.getUniqueId());
	        Party targetParty = PartyManager.getInstance().getParty(target.getUniqueId());
	        if (party != null) {
	            if (!party.getLeader().equals(player.getUniqueId())) {
	                player.sendMessage(ChatColor.RED + "You are not the leader of this party!");
	                return true;
	            }
	            if (targetParty == null) {
	                player.sendMessage(ChatColor.RED + "This player is not in a party!");
	                return true;
	            }
	            if (!targetParty.getLeader().equals(target.getUniqueId())) {
	                player.sendMessage(ChatColor.RED + "This player is not the leader of that party!");
	                return true;
	            }
	            if (targetParty.getPartyState() == PartyState.DUELING) {
	                player.sendMessage(ChatColor.RED + "This party is currently busy.");
	                return true;
	            }
	        } else if (targetParty != null) {
	            player.sendMessage(ChatColor.RED + "This player is in a party!");
	            return true;
	        }
			if (cooldowns.containsKey(player.getUniqueId())) {
				long secondsLeft = ((cooldowns.get(player.getUniqueId()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
	            if (secondsLeft > 0) {
	                player.sendMessage(ChatColor.RED + "You cant sent duel request for another " + secondsLeft + " seconds!");
	                return false;
	            }
			}
			Main.getInstance().sendDuelRequest(player, target);
			cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
		}
		return false;
	}
}
