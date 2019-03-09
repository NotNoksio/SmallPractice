package us.noks.smallpractice.commands;

import java.util.StringJoiner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.PartyManager;
import us.noks.smallpractice.objects.managers.PlayerManager;
import us.noks.smallpractice.party.Party;

public class PartyCommand implements CommandExecutor {
	
    private String[] HELP_COMMAND = new String[] {
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
            ChatColor.RED.toString() + ChatColor.BOLD + "Party Commands:",
            ChatColor.GREEN + "-> /party help " + ChatColor.GRAY + "- Displays the help menu",
            ChatColor.GREEN + "-> /party create " + ChatColor.GRAY + "- Creates a party instance",
            ChatColor.GREEN + "-> /party leave " + ChatColor.GRAY + "- Leave your current party",
            ChatColor.GREEN + "-> /party info " + ChatColor.GRAY + "- Displays your party information",
            ChatColor.RED + "-> /party join <player> " + ChatColor.GRAY + "- Join a party (invited or unlocked)",
            "",
            ChatColor.RED.toString() + ChatColor.BOLD + "Leader Commands:",
            ChatColor.GREEN + "-> /party open " + ChatColor.GRAY + "- Open your party for others to join",
            ChatColor.GREEN + "-> /party lock " + ChatColor.GRAY + "- Lock your party for others to join",
            ChatColor.RED + "-> /party invite <player> " + ChatColor.GRAY + "- Invites a player to your party",
            ChatColor.RED + "-> /party kick <player> " + ChatColor.GRAY + "- Kicks a player from your party",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"};
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
        	sender.sendMessage(ChatColor.RED + "Only player can do this command!");
        	return false;
        }
        if (args.length == 0) {
        	sender.sendMessage(this.HELP_COMMAND);
        	return true;
        }
        Player player = (Player) sender;
        PlayerManager pm = PlayerManager.get(player.getUniqueId());
        
        if (pm.getStatus() != PlayerStatus.SPAWN) {
        	player.sendMessage(ChatColor.RED + "You cant do this command in your current state!");
        	return false;
        }
        Party party = PartyManager.getInstance().getParty(player.getUniqueId());
        
        if (args[0].equalsIgnoreCase("help")) {
        	player.sendMessage(this.HELP_COMMAND);
        	return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
        	if (party != null) {
        		player.sendMessage(ChatColor.RED + "You are already in a party!");
        		return false;
        	}
        	PartyManager.getInstance().createParty(player.getUniqueId(), player.getName());
        	player.sendMessage(ChatColor.GREEN + "Party successfully created.");
        	pm.giveSpawnItem();
        	return true;
        }
        if (party == null) {
        	player.sendMessage(ChatColor.RED + "You are not in a party!");
        	return false;
        }
        if (args[0].equalsIgnoreCase("info")) {
        	Player leader = Bukkit.getPlayer(party.getLeader());
            StringJoiner members = new StringJoiner(", ");

            members.add(leader.getName());
            for (UUID memberUUID : party.getMembers()) {
                Player member = Bukkit.getPlayer(memberUUID);
                members.add(member.getName());
            }

            String[] information = new String[] {
                    ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
                    ChatColor.RED + "Party Information:",
                    ChatColor.DARK_AQUA + "Leader: " + ChatColor.YELLOW + leader.getName(),
                    ChatColor.DARK_AQUA + "Members (" + (party.getSize()) + "): " + ChatColor.GRAY + members.toString(),
                    ChatColor.DARK_AQUA + "Privacy: " + (party.isOpen() ? ChatColor.GREEN + "Open" : ChatColor.RED + "Invite-Only"),
                    ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"
            };
            player.sendMessage(information);
            return true;
        }
        if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("disband")) {
        	if (party.getLeader().equals(player.getUniqueId())) {
                PartyManager.getInstance().transferLeader(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "The party has been disbanded!");
            } else {
                PartyManager.getInstance().notifyParty(party, ChatColor.RED + player.getName() + " has left the party");
                PartyManager.getInstance().leaveParty(player.getUniqueId());
            }
        	pm.giveSpawnItem();
        	return true;
        }
        if (args[0].equalsIgnoreCase("open")) {
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
        if (args[0].equalsIgnoreCase("lock")) {
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
        if (args.length != 2) {
        	player.sendMessage(this.HELP_COMMAND);
        	return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        
        if (target == null) {
        	player.sendMessage(ChatColor.RED + "This player isnt online!");
        	return false;
        }
        if (target == player) {
        	player.sendMessage(ChatColor.RED + "You cant do that on yourself.");
        	return false;
        }
        PlayerManager tm = PlayerManager.get(target.getUniqueId());
        
        if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("accept")) {
        	if (!PartyManager.getInstance().hasParty(target.getUniqueId())) {
            	return false;
            }
        }
        
        // ITS ANNOYING :'(
        
        return false;
    }
}
