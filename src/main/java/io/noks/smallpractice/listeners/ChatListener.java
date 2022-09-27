package io.noks.smallpractice.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.party.Party;

public class ChatListener implements Listener {
	private Main main;
	public ChatListener(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onStaffOrPartyChat(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		if (event.getMessage().charAt(0) == '@' && player.hasPermission("chat.staff")) {
			String message = event.getMessage();
	      
			if (message.length() == 1) {
				return;
			}
			message = message.replaceFirst("@", "");
			for (Player staff : Bukkit.getOnlinePlayers()) {
				if (staff.hasPermission("chat.staff")) {
					event.setCancelled(true);
					staff.sendMessage(ChatColor.GREEN + "(" + ChatColor.RED + "Staff" + ChatColor.GREEN + ") " + player.getDisplayName() + ChatColor.GOLD + " » " + message);
				}
			}
			return;
		}
		if (event.getMessage().charAt(0) == '!' && this.main.getPartyManager().hasParty(player.getUniqueId())) {
			String message = event.getMessage();
	      
			if (message.length() == 1) {
				return;
			}
			message = message.replaceFirst("!", "");
			Party party = this.main.getPartyManager().getParty(player.getUniqueId());
			party.notify(ChatColor.YELLOW + "(" + ChatColor.RED + "Party" + ChatColor.YELLOW + ") " + player.getDisplayName() + ChatColor.GOLD + " » " + message);
		}
	}
}
