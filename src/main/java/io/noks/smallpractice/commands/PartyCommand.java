package io.noks.smallpractice.commands;

import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.managers.PlayerManager;
import io.noks.smallpractice.party.Party;
import io.noks.smallpractice.party.PartyState;

public class PartyCommand implements CommandExecutor {
	
    private String[] HELP_COMMAND = new String[] {
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
            ChatColor.RED.toString() + ChatColor.BOLD + "Party Commands:",
            ChatColor.GREEN + "-> /party help " + ChatColor.GRAY + "- Displays the help menu",
            ChatColor.GREEN + "-> /party create " + ChatColor.GRAY + "- Creates a party instance",
            ChatColor.GREEN + "-> /party leave " + ChatColor.GRAY + "- Leave your current party",
            ChatColor.GREEN + "-> /party info [<player>]" + ChatColor.GRAY + "- Displays your party information",
            ChatColor.GREEN + "-> /party join <player> " + ChatColor.GRAY + "- Join a party (invited or unlocked)",
            ChatColor.GREEN + "-> /party deny <player> " + ChatColor.GRAY + "- Deny a party invite",
            "",
            ChatColor.RED.toString() + ChatColor.BOLD + "Leader Commands:",
            ChatColor.GREEN + "-> /party open " + ChatColor.GRAY + "- Open your party for others to join",
            ChatColor.GREEN + "-> /party lock/close " + ChatColor.GRAY + "- Lock your party for others to join",
            ChatColor.GREEN + "-> /party invite <player> " + ChatColor.GRAY + "- Invites a player to your party",
            ChatColor.GREEN + "-> /party kick <player> " + ChatColor.GRAY + "- Kicks a player from your party",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"};
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
        	sender.sendMessage(ChatColor.RED + "Only player can do this command!");
        	return false;
        }
        if (args.length == 0 || args.length > 2) {
        	sender.sendMessage(this.HELP_COMMAND);
        	return true;
        }
        final Player player = (Player) sender;
        final PlayerManager pm = PlayerManager.get(player.getUniqueId());
        
        if (pm.getStatus() != PlayerStatus.SPAWN) {
        	player.sendMessage(ChatColor.RED + "You cant do this command in your current state!");
        	return false;
        }
        final Party party = Main.getInstance().getPartyManager().getParty(player.getUniqueId());
        
        if (args[0].equalsIgnoreCase("help")) {
        	player.sendMessage(this.HELP_COMMAND);
        	return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
        	if (party != null) {
        		player.sendMessage(ChatColor.RED + "You are already in a party!");
        		return false;
        	}
        	Main.getInstance().getPartyManager().createParty(player.getUniqueId(), player.getName());
        	player.sendMessage(ChatColor.GREEN + "Party successfully created.");
        	Main.getInstance().getItemManager().giveSpawnItem(player);
        	return true;
        }
        if (args.length == 1) {
	        if (args[0].equalsIgnoreCase("info")) {
	        	if (party == null) {
	        		player.sendMessage(ChatColor.RED + "You are not in a party!");
	        		return false;
	        	}
	        	final Player leader = Bukkit.getPlayer(party.getLeader());
	        	final StringJoiner members = new StringJoiner(", ");
	
	            members.add(leader.getName());
	            for (UUID memberUUID : party.getMembers()) {
	                Player member = Bukkit.getPlayer(memberUUID);
	                members.add(member.getName());
	            }
	
	            final String[] information = new String[] {
	                    ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
	                    ChatColor.RED + "Your party informations:",
	                    ChatColor.DARK_AQUA + "Leader: " + ChatColor.YELLOW + leader.getName(),
	                    ChatColor.DARK_AQUA + "Members (" + (party.getSize()) + "): " + ChatColor.GRAY + members.toString() + (party.getSize() == 2 ? ChatColor.DARK_GRAY + " (" + party.getPartyEloManager().getGlobal() + ")" : ""),
	                    ChatColor.DARK_AQUA + "Privacy: " + (party.isOpen() ? ChatColor.GREEN + "Open" : ChatColor.RED + "Invite-Only"),
	                    ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"
	            };
	            player.sendMessage(information);
	            return true;
	        }
	        if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("disband")) {
	        	if (party == null) {
	        		player.sendMessage(ChatColor.RED + "You are not in a party!");
	        		return false;
	        	}
	        	if (party.getPartyState() == PartyState.QUEUING) {
	        		Main.getInstance().getQueueManager().quitQueue(Bukkit.getPlayer(party.getLeader()));
	        		if (args[0].equalsIgnoreCase("leave")) {
	        			party.notify(ChatColor.RED + "Your party has been removed from the queue! Your teammate has left your party.");
	        		}
	        	}
	        	if (party.getLeader().equals(player.getUniqueId())) {
	        		Main.getInstance().getPartyManager().transferLeader(player.getUniqueId());
	            } else {
	            	party.notify(ChatColor.RED + player.getName() + " has left the party");
	            	Main.getInstance().getPartyManager().leaveParty(player.getUniqueId());
	            }
	        	Main.getInstance().getItemManager().giveSpawnItem(player);
	        	return true;
	        }
	        if (args[0].equalsIgnoreCase("open")) {
	        	if (party == null) {
	        		player.sendMessage(ChatColor.RED + "You are not in a party!");
	        		return false;
	        	}
	        	if (!party.getLeader().equals(player.getUniqueId())) {
	        		player.sendMessage(ChatColor.RED + "You are not the leader of the party!");
	        		return false;
	        	}
	        	if (party.isOpen()) {
	        		player.sendMessage(ChatColor.RED + "Your party is already open, please do /party lock to lock your party.");
	        		return false;
	        	}
	        	party.setOpen(true);
	            player.sendMessage(ChatColor.GREEN + "You have opened your party!");
	            return true;
	        }
	        if (args[0].equalsIgnoreCase("lock") || args[0].equalsIgnoreCase("close")) {
	        	if (party == null) {
	        		player.sendMessage(ChatColor.RED + "You are not in a party!");
	        		return false;
	        	}
	        	if (!party.getLeader().equals(player.getUniqueId())) {
	        		player.sendMessage(ChatColor.RED + "You are not the leader of the party!");
	        		return false;
	        	}
	        	if (!party.isOpen()) {
	        		player.sendMessage(ChatColor.RED + "Your party is already lock, please do /party open to open your party.");
	        		return false;
	        	}
	        	party.setOpen(false);
	            player.sendMessage(ChatColor.GREEN + "You have locked your party!");
	            return true;
	        }
	        return false;
        }
        if (args.length == 2) {
        	final Player target = Bukkit.getPlayer(args[1]);
            
            if (target == null) {
            	player.sendMessage(ChatColor.RED + "This player isnt online!");
            	return false;
            }
            if (target == player) {
            	player.sendMessage(ChatColor.RED + "You cant do that on yourself.");
            	return false;
            }
            final PlayerManager tm = PlayerManager.get(target.getUniqueId());
            final Party targetParty = Main.getInstance().getPartyManager().getParty(target.getUniqueId());
            
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("accept")) {
            	if (targetParty == null) {
            		player.sendMessage(ChatColor.RED + "This party has expired!");
                	return false;
            	}
            	if (Main.getInstance().getPartyManager().hasParty(player.getUniqueId())) {
            		player.sendMessage(ChatColor.RED + "You are already in a party!");
                	return false;
                }
            	if (targetParty.isOpen()) {
            		Main.getInstance().getPartyManager().joinParty(targetParty.getLeader(), player.getUniqueId());
            		targetParty.notify(ChatColor.GREEN + player.getName() + " has joined the party");
                    player.sendMessage(ChatColor.GREEN + "You have joined the party!");
                    Main.getInstance().getItemManager().giveSpawnItem(target);
            		return true;
            	}
            	if (!tm.hasInvited(player.getUniqueId())) {
            		player.sendMessage(ChatColor.RED + "You are not invited to this party!");
            		return false;
            	}
            	Main.getInstance().getRequestManager().acceptPartyInvite(player, target);
                return true;
            }
            if (args[0].equalsIgnoreCase("invite")) {
            	if (party == null) {
            		player.sendMessage(ChatColor.RED + "You are not in a party!");
            		return false;
            	}
            	if (Main.getInstance().getPartyManager().hasParty(target.getUniqueId())) {
            		player.sendMessage(ChatColor.RED + "This player is already in a party!");
                	return false;
                }
            	if (Main.getInstance().getPartyManager().hasParty(target.getUniqueId())) {
            		player.sendMessage(ChatColor.RED + "This player is already in a party!");
            		return false;
            	}
            	Main.getInstance().getRequestManager().sendPartyInvite(player, target);
            	return true;
            }
            if (args[0].equalsIgnoreCase("deny")) {
            	if (targetParty == null) {
            		player.sendMessage(ChatColor.RED + "This party has expired!");
                	return false;
            	}
            	Main.getInstance().getRequestManager().denyPartyInvite(player, target);
            	return true;
            }
            if (args[0].equalsIgnoreCase("kick")) {
            	if (party == null) {
            		player.sendMessage(ChatColor.RED + "You are not in a party!");
            		return false;
            	}
            	if (!party.getLeader().equals(player.getUniqueId())) {
	        		player.sendMessage(ChatColor.RED + "You are not the leader of the party!");
	        		return false;
	        	}
            	if (!party.getMembers().contains(target.getUniqueId())) {
            		player.sendMessage(ChatColor.RED + "This player is not in your party!");
            		return false;
            	}
            	party.notify(ChatColor.RED + target.getName() + " has been kicked from the party!");
            	Main.getInstance().getPartyManager().leaveParty(target.getUniqueId());
                Main.getInstance().getItemManager().giveSpawnItem(target);
            	return true;
            }
            if (args[0].equalsIgnoreCase("info")) {
	        	if (targetParty == null) {
	        		player.sendMessage(ChatColor.RED + "This player is not in a party!");
	        		return false;
	        	}
	        	final Player leader = Bukkit.getPlayer(targetParty.getLeader());
	        	final StringJoiner members = new StringJoiner(", ");
	
	            members.add(leader.getName());
	            for (UUID memberUUID : targetParty.getMembers()) {
	                Player member = Bukkit.getPlayer(memberUUID);
	                members.add(member.getName());
	            }
	
	            final String[] information = new String[] {
	                    ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
	                    ChatColor.RED + leader.getName() + "'s party informations:",
	                    ChatColor.DARK_AQUA + "Leader: " + ChatColor.YELLOW + leader.getName(),
	                    ChatColor.DARK_AQUA + "Members (" + (targetParty.getSize()) + "): " + ChatColor.GRAY + members.toString() + (party.getSize() == 2 ? ChatColor.DARK_GRAY + " (" + targetParty.getPartyEloManager().getGlobal() + ")" : ""),
	                    ChatColor.DARK_AQUA + "Privacy: " + (targetParty.isOpen() ? ChatColor.GREEN + "Open" : ChatColor.RED + "Invite-Only"),
	                    ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"
	            };
	            player.sendMessage(information);
	            return true;
	        }
        }
        return false;
    }
}
