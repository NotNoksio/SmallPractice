package io.noks.smallpractice.listeners;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import io.noks.smallpractice.Main;
import io.noks.smallpractice.objects.managers.PlayerManager;
import io.noks.smallpractice.party.Party;

public class ChatListener implements Listener {
	private Main main;
	public ChatListener(Main plugin) {
		this.main = plugin;
	    this.main.getServer().getPluginManager().registerEvents(this, this.main);
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		final Player player = event.getPlayer();
		PlayerManager pm = PlayerManager.get(player.getUniqueId());
		String prefix = pm.getColoredPrefix() + "%1$s" + pm.getColoredSuffix() + ChatColor.RESET;
		event.setFormat(prefix + ChatColor.WHITE + ": %2$s");
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPlayerGetMentioned(AsyncPlayerChatEvent event) {
		final String message = event.getMessage();
		Iterator<Player> iterator = event.getRecipients().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (!message.matches(".*\\b(?i)" + player.getName() + "\\b.*")) {
				continue;
			}
			if (player.getName() == event.getPlayer().getName()) {
				continue;
			}
			String mentionMessage = event.getMessage().replaceAll("\\b(?i)" + player.getName() + "\\b", ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD + player.getName() + ChatColor.RESET);
			player.sendMessage(String.format(event.getFormat(), event.getPlayer().getName(), mentionMessage));
			iterator.remove();
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onStaffOrPartyChat(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		final PlayerManager pm = PlayerManager.get(player.getUniqueId());
		if (event.getMessage().charAt(0) == '@' && player.hasPermission("chat.staff")) {
			String message = event.getMessage();
	      
			if (message.length() == 1) {
				return;
			}
			message = message.replaceFirst("@", "");
			for (Player staff : Bukkit.getOnlinePlayers()) {
				if (staff.hasPermission("chat.staff")) {
					event.setCancelled(true);
					staff.sendMessage(ChatColor.GREEN + "(" + ChatColor.RED + "Staff" + ChatColor.GREEN + ") " + pm.getPrefixColors() + player.getName() + ChatColor.GOLD + " » " + message);
				}
			}
		}
		if (event.getMessage().charAt(0) == '!' && this.main.getPartyManager().hasParty(player.getUniqueId())) {
			String message = event.getMessage();
	      
			if (message.length() == 1) {
				return;
			}
			message = message.replaceFirst("!", "");
			Party party = this.main.getPartyManager().getParty(player.getUniqueId());
			party.notify(ChatColor.YELLOW + "(" + ChatColor.RED + "Party" + ChatColor.YELLOW + ") " + pm.getPrefixColors() + player.getName() + ChatColor.GOLD + " » " + message);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void translateColorWhenSignPlaced(SignChangeEvent event) {
		if (!event.getPlayer().isOp()) {
			return;
		}
		for (int i = 0; i < 4; i++) {
			if (event.getLine(i).length() == 0) continue;
			event.setLine(i, ChatColor.translateAlternateColorCodes('&', event.getLine(i)));  
		}
	}
}
