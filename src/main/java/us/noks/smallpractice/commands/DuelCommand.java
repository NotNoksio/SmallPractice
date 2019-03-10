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
import us.noks.smallpractice.objects.managers.PartyManager;
import us.noks.smallpractice.objects.managers.RequestManager;
import us.noks.smallpractice.party.Party;
import us.noks.smallpractice.party.PartyState;
import us.noks.smallpractice.utils.Messages;

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
				player.sendMessage(Messages.getInstance().PLAYER_NOT_ONLINE);
				return false;
			}
			if (target == player) {
				player.sendMessage(Messages.getInstance().NOT_YOURSELF);
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
			if (cooldowns.containsKey(player.getUniqueId())) {
				long secondsLeft = ((cooldowns.get(player.getUniqueId()) / 1000) + cooldownTime) - (System.currentTimeMillis() / 1000);
	            if (secondsLeft > 0) {
	                player.sendMessage(ChatColor.RED + "You cant sent duel request for another " + secondsLeft + " seconds!");
	                return false;
	            }
			}
			RequestManager.getInstance().sendDuelRequest(player, target);
			cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
		}
		return false;
	}
}
