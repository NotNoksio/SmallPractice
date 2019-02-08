package us.noks.smallpractice.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import us.noks.smallpractice.enums.PlayerStatus;
import us.noks.smallpractice.objects.managers.PartyManager;
import us.noks.smallpractice.objects.managers.PlayerManager;

public class PartyCommand implements CommandExecutor
{
    private String[] HELP_COMMAND = new String[] {
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
            ChatColor.RED.toString() + ChatColor.BOLD + "Party Commands:",
            ChatColor.YELLOW + "-> /party help " + ChatColor.GRAY + "- Displays the help menu",
            ChatColor.YELLOW + "-> /party create " + ChatColor.GRAY + "- Creates a party instance",
            ChatColor.YELLOW + "-> /party leave " + ChatColor.GRAY + "- Leave your current party",
            ChatColor.YELLOW + "-> /party info " + ChatColor.GRAY + "- Displays your party information",
            ChatColor.YELLOW + "-> /party join <player> " + ChatColor.GRAY + "- Join a party (invited or unlocked)",
            "",
            ChatColor.RED.toString() + ChatColor.BOLD + "Leader Commands:",
            ChatColor.YELLOW + "-> /party open " + ChatColor.GRAY + "- Open your party for others to join",
            ChatColor.YELLOW + "-> /party lock " + ChatColor.GRAY + "- Lock your party for others to join",
            ChatColor.YELLOW + "-> /party invite <player> " + ChatColor.GRAY + "- Invites a player to your party",
            ChatColor.YELLOW + "-> /party kick <player> " + ChatColor.GRAY + "- Kicks a player from your party",
            ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------"};
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
        	return false;
        }
        if (args.length == 0) {
        	sender.sendMessage(this.HELP_COMMAND);
        	return true;
        }
        Player player = (Player) sender;
        PlayerManager pm = PlayerManager.get(player);
        
        if (pm.getStatus() != PlayerStatus.SPAWN) {
        	return false;
        }
        if (!PartyManager.getInstance().hasParty(player.getUniqueId())) {
        	return false;
        }
        if (args[0].equalsIgnoreCase("help")) {
        	player.sendMessage(this.HELP_COMMAND);
        	return true;
        }
        
        // ITS ANNOYING :'(
        
        return false;
    }
}
