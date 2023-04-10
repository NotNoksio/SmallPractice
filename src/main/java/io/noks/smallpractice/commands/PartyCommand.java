package io.noks.smallpractice.commands;

import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.enums.PlayerStatus;
import io.noks.smallpractice.objects.PlayerSettings;
import io.noks.smallpractice.objects.managers.PlayerManager;
import io.noks.smallpractice.party.Party;
import io.noks.smallpractice.party.PartyState;

public class PartyCommand implements CommandExecutor {
	private Main main;
    private String[] HELP_COMMAND;
    public PartyCommand(Main main) {
    	this.main = main;
    	main.getCommand("party").setExecutor(this);
    	this.HELP_COMMAND = new String[] {
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
                ChatColor.GREEN + "-> /party confirm " + ChatColor.GRAY + "- Register your party to access ranked matches",
                ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"};
    }
    
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
        final Party party = this.main.getPartyManager().getParty(player.getUniqueId());
        
        if (args[0].equalsIgnoreCase("help")) {
        	player.sendMessage(this.HELP_COMMAND);
        	return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
        	if (party != null) {
        		player.sendMessage(ChatColor.RED + "You are already in a party!");
        		return false;
        	}
        	this.main.getPartyManager().createParty(player.getUniqueId(), player.getName());
        	player.sendMessage(ChatColor.GREEN + "Party successfully created.");
        	this.main.getItemManager().giveSpawnItem(player);
        	return true;
        }
        if (args.length == 1) {
	        if (args[0].equalsIgnoreCase("info")) {
	        	if (party == null) {
	        		player.sendMessage(ChatColor.RED + "You are not in a party!");
	        		return false;
	        	}
	        	final Player leader = this.main.getServer().getPlayer(party.getLeader());
	        	final StringJoiner members = new StringJoiner(", ");
	
	            members.add(leader.getName());
	            for (UUID memberUUID : party.getMembers()) {
	                Player member = this.main.getServer().getPlayer(memberUUID);
	                members.add(member.getName());
	            }
	
	            final String[] information = new String[] {
	                    ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
	                    ChatColor.RED + "Your party informations:",
	                    ChatColor.DARK_AQUA + "Leader: " + ChatColor.YELLOW + leader.getName(),
	                    ChatColor.DARK_AQUA + "Members (" + (party.getSize()) + "): " + ChatColor.GRAY + members.toString() + (party.getPartyEloManager() != null ? ChatColor.DARK_GRAY + " (" + party.getPartyEloManager().getGlobal() + ")" : ""),
	                    ChatColor.DARK_AQUA + "Privacy: " + (party.isOpen() ? ChatColor.GREEN + "Open" : ChatColor.RED + "Invite-Only"),
	                    ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"
	            };
	            player.sendMessage(information);
	            return true;
	        }
	        if (args[0].equalsIgnoreCase("leave")) {
	        	if (party == null) {
	        		player.sendMessage(ChatColor.RED + "You are not in a party!");
	        		return false;
	        	}
	        	final boolean isLeader = party.getLeader().equals(player.getUniqueId());
	        	if (party.getPartyState() == PartyState.QUEUING) {
	        		this.main.getQueueManager().quitQueue(this.main.getServer().getPlayer(party.getLeader()), false);
	        		party.notify(ChatColor.RED + "Your party has been removed from the queue! Your " + (!isLeader ? "teammate has left your party" : "leader has left the party") + ".");
	        	}
	        	if (isLeader) {
	        		this.main.getPartyManager().transferLeader(player.getUniqueId());
	            } else {
	            	party.notify(ChatColor.RED + player.getName() + " has left the party");
	            	this.main.getPartyManager().leaveParty(player.getUniqueId());
	            }
	        	this.main.getItemManager().giveSpawnItem(player);
	        	return true;
	        }
	        if (args[0].equalsIgnoreCase("disband")) {
	        	if (party == null) {
	        		player.sendMessage(ChatColor.RED + "You are not in a party!");
	        		return false;
	        	}
	        	if (!party.getLeader().equals(player.getUniqueId())) {
	        		player.sendMessage(ChatColor.RED + "You are not the leader of the party!");
	        		return false;
	        	}
	        	if (party.getPartyState() == PartyState.QUEUING) {
	        		this.main.getQueueManager().quitQueue(this.main.getServer().getPlayer(party.getLeader()), false);
	        		party.notify(ChatColor.RED + "Your party has been removed from the queue! Your leader has disband the party.");
	        	}
	        	this.main.getPartyManager().destroyParty(party.getLeader());
	        	this.main.getItemManager().giveSpawnItem(player);
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
	        if (args[0].equalsIgnoreCase("confirm")) {
	        	if (party == null) {
	        		player.sendMessage(ChatColor.RED + "You are not in a party!");
	        		return false;
	        	}
	        	if (!party.getLeader().equals(player.getUniqueId())) {
	        		player.sendMessage(ChatColor.RED + "You are not the leader of the party!");
	        		return false;
	        	}
	        	if (party.getSize() != 2) {
	        		player.sendMessage(ChatColor.RED + "Your party must contain only 2 players!");
	        		return false;
	        	}
	        	if (party.getPartyEloManager() != null) {
	        		player.sendMessage(ChatColor.RED + "Your party has already been confirmed!");
	        		return false;
	        	}
	        	party.initElo();
	        	party.notify(ChatColor.GREEN + "Your party has been confirmed by the leader!");
	        	return true;
	        }
	        player.sendMessage(this.HELP_COMMAND);
	        return false;
        }
        if (args.length == 2) {
        	final Player target = this.main.getServer().getPlayer(args[1]);
            
            if (target == null) {
            	player.sendMessage(ChatColor.RED + "This player isnt online!");
            	return false;
            }
            if (target == player) {
            	player.sendMessage(ChatColor.RED + "You cant do that on yourself.");
            	return false;
            }
            final PlayerManager tm = PlayerManager.get(target.getUniqueId());
            final Party targetParty = this.main.getPartyManager().getParty(target.getUniqueId());
            
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("accept")) {
            	if (targetParty == null) {
            		player.sendMessage(ChatColor.RED + "This party has expired!");
                	return false;
            	}
            	if (this.main.getPartyManager().hasParty(player.getUniqueId())) {
            		player.sendMessage(ChatColor.RED + "You are already in a party!");
                	return false;
                }
            	if (targetParty.isOpen()) {
            		this.main.getPartyManager().joinParty(targetParty.getLeader(), player.getUniqueId());
            		targetParty.notify(ChatColor.GREEN + player.getName() + " has joined the party");
                    player.sendMessage(ChatColor.GREEN + "You have joined the party!");
                    this.main.getItemManager().giveSpawnItem(player);
            		return true;
            	}
            	if (!tm.hasInvited(player.getUniqueId())) {
            		player.sendMessage(ChatColor.RED + "You are not invited to this party!");
            		return false;
            	}
            	this.main.getRequestManager().acceptPartyInvite(player, target);
                return true;
            }
            if (args[0].equalsIgnoreCase("invite")) {
            	if (party == null) {
            		player.sendMessage(ChatColor.RED + "You are not in a party!");
            		return false;
            	}
            	final PlayerSettings ts = tm.getSettings();
        		if (!ts.isPartyInviteToggled()) {
        			sender.sendMessage(ChatColor.RED + "This player doesn't allow party invite!");
        			return false;
        		}
            	if (this.main.getPartyManager().hasParty(target.getUniqueId())) {
            		player.sendMessage(ChatColor.RED + "This player is already in a party!");
                	return false;
                }
            	this.main.getRequestManager().sendPartyInvite(party, target);
            	return true;
            }
            if (args[0].equalsIgnoreCase("deny")) {
            	if (targetParty == null) {
            		player.sendMessage(ChatColor.RED + "This party has expired!");
                	return false;
            	}
            	this.main.getRequestManager().denyPartyInvite(player, target);
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
            	if (party.getPartyState() == PartyState.QUEUING) {
            		player.sendMessage(ChatColor.RED + "You can't do that while queuing!");
            		return false;
            	}
            	if (!party.getMembers().contains(target.getUniqueId())) {
            		player.sendMessage(ChatColor.RED + "This player is not in your party!");
            		return false;
            	}
            	party.notify(ChatColor.RED + target.getName() + " has been kicked from the party!");
            	this.main.getPartyManager().leaveParty(target.getUniqueId());
            	this.main.getItemManager().giveSpawnItem(target);
            	return true;
            }
            if (args[0].equalsIgnoreCase("info")) {
	        	if (targetParty == null) {
	        		player.sendMessage(ChatColor.RED + "This player is not in a party!");
	        		return false;
	        	}
	        	final Player leader = this.main.getServer().getPlayer(targetParty.getLeader());
	        	final StringJoiner members = new StringJoiner(", ");
	
	            members.add(leader.getName());
	            for (UUID memberUUID : targetParty.getMembers()) {
	                Player member = this.main.getServer().getPlayer(memberUUID);
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
